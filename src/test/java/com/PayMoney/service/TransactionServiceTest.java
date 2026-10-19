
package com.PayMoney.service;

import com.PayMoney.DTO.TransferRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.InsufficientBalanceException;
import com.PayMoney.Exception.InvalidTransactionException;
import com.PayMoney.Mapper.TransactionMapper;
import com.PayMoney.Repository.IdempotencyRepository;
import com.PayMoney.Repository.TransactionRepository;
import com.PayMoney.Repository.WalletRepository;

import com.PayMoney.Service.AuditLogService;
import com.PayMoney.Service.AuthService;
import com.PayMoney.Service.FraudService;
import com.PayMoney.Service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AuthService authService;

    @Mock
    private FraudService fraudService;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private AuditLogService auditLogService;

    private TransferRequestDTO createRequest(
            Long senderId, Long receiverId, String amount) {

        TransferRequestDTO request = new TransferRequestDTO();
        request.setSenderWalletId(senderId);
        request.setReceiverWalletId(receiverId);
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    private UserEntity createUser(Long userId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        return user;
    }

    private WalletEntity createWallet(
            Long walletId, UserEntity user, String balance) {

        WalletEntity wallet = new WalletEntity();
        wallet.setWalletId(walletId);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal(balance));
        return wallet;
    }

    private String requestHash(TransferRequestDTO request) {
        return ReflectionTestUtils.invokeMethod(
                transactionService,
                "generateRequestHash",
                request
        );
    }

    @Test
    void transfer_shouldThrowException_whenBalanceIsInsufficient() {
        UserEntity user = createUser(1L);
        WalletEntity sender = createWallet(1L, user, "1000");
        WalletEntity receiver = createWallet(2L, createUser(2L), "500");

        TransferRequestDTO request = createRequest(1L, 2L, "2000");

        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());
        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(sender));
        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(receiver));
        when(fraudService.isSuspicious(1L))
                .thenReturn(false);

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
        verify(transactionRepository, never())
                .save(any(TransactionEntity.class));
        verify(idempotencyRepository, never())
                .save(any(IdempotencyEntity.class));
    }

    @Test
    void transfer_shouldTransferMoney_whenBalanceIsSufficient() {
        UserEntity user = createUser(1L);
        WalletEntity sender = createWallet(1L, user, "5000");
        WalletEntity receiver = createWallet(2L, createUser(2L), "1000");

        TransferRequestDTO request = createRequest(1L, 2L, "2000");

        TransactionEntity savedTransaction = new TransactionEntity();
        savedTransaction.setTransactionId(100L);

        TransferResponseDTO response = new TransferResponseDTO(
                100L,
                1L,
                2L,
                new BigDecimal("2000"),
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                LocalDateTime.now()
        );

        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());
        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(sender));
        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(receiver));
        when(fraudService.isSuspicious(1L)).thenReturn(false);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenReturn(savedTransaction);
        when(transactionMapper.toDTO(savedTransaction)).thenReturn(response);

        TransferResponseDTO result =
                transactionService.transfer(request, "key123");

        assertEquals(response, result);
        assertEquals(new BigDecimal("3000"), sender.getBalance());
        assertEquals(new BigDecimal("3000"), receiver.getBalance());

        verify(walletRepository).save(sender);
        verify(walletRepository).save(receiver);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(idempotencyRepository).save(any(IdempotencyEntity.class));
        verify(auditLogService).log(
                eq(1L),
                eq("TRANSFER"),
                anyString()
        );
    }

    @Test
    void transfer_shouldThrowException_whenSenderAndReceiverAreSame() {
        TransferRequestDTO request = createRequest(1L, 1L, "1000");

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(idempotencyRepository);
    }

    @Test
    void transfer_shouldReturnExistingTransaction_whenIdempotencyKeyAlreadyExists() {
        UserEntity user = createUser(1L);
        TransferRequestDTO request = createRequest(1L, 2L, "1000");

        IdempotencyEntity existingKey = new IdempotencyEntity();
        existingKey.setIdempotencyKey("key123");
        existingKey.setUserId(1L);
        existingKey.setTransactionId(100L);
        existingKey.setRequestHash(requestHash(request));

        TransactionEntity existingTransaction = new TransactionEntity();
        existingTransaction.setTransactionId(100L);

        TransferResponseDTO response = new TransferResponseDTO(
                100L,
                1L,
                2L,
                new BigDecimal("1000"),
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                LocalDateTime.now()
        );

        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.of(existingKey));
        when(transactionRepository.findById(100L))
                .thenReturn(Optional.of(existingTransaction));
        when(transactionMapper.toDTO(existingTransaction))
                .thenReturn(response);

        TransferResponseDTO result =
                transactionService.transfer(request, "key123");

        assertEquals(response, result);

        verify(transactionRepository, never())
                .save(any(TransactionEntity.class));
        verify(idempotencyRepository, never())
                .save(any(IdempotencyEntity.class));
        verifyNoInteractions(walletRepository);
    }

    @Test
    void transfer_shouldThrowException_whenSenderWalletDoesNotBelongToUser() {
        UserEntity walletOwner = createUser(2L);
        UserEntity authenticatedUser = createUser(1L);

        WalletEntity sender = createWallet(1L, walletOwner, "5000");
        WalletEntity receiver = createWallet(2L, createUser(3L), "1000");

        TransferRequestDTO request = createRequest(1L, 2L, "1000");

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());
        when(walletRepository.findWalletForUpdate(1L))
                .thenReturn(Optional.of(sender));
        when(walletRepository.findWalletForUpdate(2L))
                .thenReturn(Optional.of(receiver));

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
        verify(transactionRepository, never())
                .save(any(TransactionEntity.class));
    }

    @Test
    void transfer_shouldThrowException_whenIdempotencyKeyIsBlank() {
        TransferRequestDTO request = createRequest(1L, 2L, "100");

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(request, " ")
        );

        verifyNoInteractions(authService);
        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(idempotencyRepository);
    }

    @Test
    void transfer_shouldThrowException_whenAmountIsZero() {
        TransferRequestDTO request = createRequest(1L, 2L, "0");

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verifyNoInteractions(walletRepository);
        verify(transactionRepository, never())
                .save(any(TransactionEntity.class));
    }

    @Test
    void transfer_shouldThrowException_whenAmountIsNegative() {
        TransferRequestDTO request = createRequest(1L, 2L, "-100");

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(request, "key123")
        );

        verifyNoInteractions(walletRepository);
        verify(transactionRepository, never())
                .save(any(TransactionEntity.class));
    }

    @Test
    void transfer_shouldRejectSameKey_whenRequestDetailsAreDifferent() {
        UserEntity user = createUser(1L);

        TransferRequestDTO originalRequest =
                createRequest(1L, 2L, "1000");

        TransferRequestDTO differentRequest =
                createRequest(1L, 2L, "2000");

        IdempotencyEntity existingKey = new IdempotencyEntity();
        existingKey.setIdempotencyKey("key123");
        existingKey.setUserId(1L);
        existingKey.setTransactionId(100L);
        existingKey.setRequestHash(requestHash(originalRequest));

        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(idempotencyRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.of(existingKey));

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(differentRequest, "key123")
        );

        verify(transactionRepository, never())
                .save(any(TransactionEntity.class));
        verify(idempotencyRepository, never())
                .save(any(IdempotencyEntity.class));
        verifyNoInteractions(walletRepository);
    }
}
