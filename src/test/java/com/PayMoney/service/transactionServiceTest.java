package com.PayMoney.service;

import com.PayMoney.DTO.transferRequestDTO;
import com.PayMoney.DTO.transferResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.insufficientBalanceException;
import com.PayMoney.Exception.invalidTransactionException;
import com.PayMoney.Mapper.transactionMapper;
import com.PayMoney.Repository.idempotencyRepository;
import com.PayMoney.Repository.transactionRepository;
import com.PayMoney.Repository.walletRepository;
import com.PayMoney.Service.auditLogService;
import com.PayMoney.Service.authService;
import com.PayMoney.Service.fraudService;
import com.PayMoney.Service.transactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class transactionServiceTest {

    @InjectMocks
    private transactionService transactionService;

    @Mock
    private walletRepository walletRepository;

    @Mock
    private transactionRepository transactionRepository;

    @Mock
    private transactionMapper transactionMapper;

    @Mock
    private authService authService;

    @Mock
    private fraudService fraudService;

    @Mock
    private idempotencyRepository idempotencyRepository;

    @Mock
    private auditLogService auditLogService;


    @Test
    void transfer_shouldThrowException_whenBalanceIsInsufficient() {

        userEntity user = new userEntity();
        user.setUserId(1L);

        walletEntity sender = new walletEntity();
        sender.setWalletId(1L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        walletEntity receiver = new walletEntity();
        receiver.setWalletId(2L);
        receiver.setUser(user);
        receiver.setBalance(new BigDecimal("500"));

        transferRequestDTO request = new transferRequestDTO();
        request.setSenderWalletId(1L);
        request.setReceiverWalletId(2L);
        request.setAmount(new BigDecimal("2000"));

        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(sender));

        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(receiver));

        when(authService.getAuthenticatedUser())
                .thenReturn(user);

        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());

        when(fraudService.isSuspicious(1L, new BigDecimal("2000")))
                .thenReturn(false);

        assertThrows(
                insufficientBalanceException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_shouldTransferMoney_whenBalanceIsSufficient() {

        userEntity user = new userEntity();
        user.setUserId(1L);

        walletEntity sender = new walletEntity();
        sender.setWalletId(1L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("5000"));

        walletEntity receiver = new walletEntity();
        receiver.setWalletId(2L);
        receiver.setUser(user);
        receiver.setBalance(new BigDecimal("1000"));

        transferRequestDTO request = new transferRequestDTO();
        request.setSenderWalletId(1L);
        request.setReceiverWalletId(2L);
        request.setAmount(new BigDecimal("2000"));

        transactionEntity savedTransaction = new transactionEntity();
        savedTransaction.setTransactionId(100L);

        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(sender));

        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(receiver));

        when(authService.getAuthenticatedUser())
                .thenReturn(user);

        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());

        when(fraudService.isSuspicious(1L, new BigDecimal("2000")))
                .thenReturn(false);

        when(transactionRepository.save(any(transactionEntity.class)))
                .thenReturn(savedTransaction);

        transactionService.transfer(request, "key123");

        assertEquals(
                new BigDecimal("3000"),
                sender.getBalance()
        );

        assertEquals(
                new BigDecimal("3000"),
                receiver.getBalance()
        );

        verify(walletRepository).save(sender);
        verify(walletRepository).save(receiver);

        verify(transactionRepository).save(any(transactionEntity.class));

        verify(idempotencyRepository).save(any(idempotencyEntity.class));

        verify(auditLogService).log(
                eq(1L),
                eq("TRANSFER"),
                anyString()
        );
    }

    @Test
    void transfer_shouldThrowException_whenSenderAndReceiverAreSame() {

        transferRequestDTO request = new transferRequestDTO();

        request.setSenderWalletId(1L);
        request.setReceiverWalletId(1L);
        request.setAmount(new BigDecimal("1000"));

        assertThrows(
                invalidTransactionException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_shouldReturnExistingTransaction_whenIdempotencyKeyAlreadyExists() {

        userEntity user = new userEntity();
        user.setUserId(1L);

        transferRequestDTO request = new transferRequestDTO();
        request.setSenderWalletId(1L);
        request.setReceiverWalletId(2L);
        request.setAmount(new BigDecimal("1000"));

        idempotencyEntity existingKey = new idempotencyEntity();
        existingKey.setIdempotencyKey("key123");
        existingKey.setUserId(1L);
        existingKey.setTransactionId(100L);

        transactionEntity existingTransaction = new transactionEntity();
        existingTransaction.setTransactionId(100L);

        transferResponseDTO response = new transferResponseDTO(
                100L,
                1L,
                2L,
                new BigDecimal("1000"),
                transactionType.TRANSFER,
                transactionStatus.SUCCESS,
                LocalDateTime.now()
        );

        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(new walletEntity()));

        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(new walletEntity()));

        when(authService.getAuthenticatedUser())
                .thenReturn(user);

        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.of(existingKey));

        when(transactionRepository.findById(100L))
                .thenReturn(Optional.of(existingTransaction));

        when(transactionMapper.toDTO(existingTransaction))
                .thenReturn(response);

        transferResponseDTO result =
                transactionService.transfer(request, "key123");

        assertEquals(response, result);

        verify(transactionRepository, never())
                .save(any(transactionEntity.class));

        verify(idempotencyRepository, never())
                .save(any(idempotencyEntity.class));
    }

    @Test
    void transfer_shouldThrowException_whenSenderWalletDoesNotBelongToUser() {

        userEntity walletOwner = new userEntity();
        walletOwner.setUserId(2L);

        userEntity authenticatedUser = new userEntity();
        authenticatedUser.setUserId(1L);

        walletEntity sender = new walletEntity();
        sender.setWalletId(1L);
        sender.setUser(walletOwner);
        sender.setBalance(new BigDecimal("5000"));

        walletEntity receiver = new walletEntity();
        receiver.setWalletId(2L);
        receiver.setUser(authenticatedUser);
        receiver.setBalance(new BigDecimal("1000"));

        transferRequestDTO request = new transferRequestDTO();
        request.setSenderWalletId(1L);
        request.setReceiverWalletId(2L);
        request.setAmount(new BigDecimal("1000"));

        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(sender));

        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(receiver));

        when(authService.getAuthenticatedUser())
                .thenReturn(authenticatedUser);

        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());

        assertThrows(
                invalidTransactionException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verify(walletRepository, never())
                .save(any(walletEntity.class));

        verify(transactionRepository, never())
                .save(any(transactionEntity.class));
    }
}