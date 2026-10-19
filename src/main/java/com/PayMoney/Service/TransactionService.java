package com.PayMoney.Service;

import com.PayMoney.DTO.TransferRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.InsufficientBalanceException;
import com.PayMoney.Exception.InvalidTransactionException;
import com.PayMoney.Exception.TransactionNotFoundException;
import com.PayMoney.Exception.WalletNotFoundException;
import com.PayMoney.Mapper.TransactionMapper;
import com.PayMoney.Repository.IdempotencyRepository;
import com.PayMoney.Repository.TransactionRepository;
import com.PayMoney.Repository.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private FraudService fraudService;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private AuditLogService auditLogService;



    @Transactional
    public TransferResponseDTO transfer(
            TransferRequestDTO request,
            String idempotencyKey) {

        if (request == null) {
            throw new InvalidTransactionException(
                    "Transfer request is required");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidTransactionException(
                    "Idempotency-Key header is required");
        }

        if (idempotencyKey.length() > 64) {
            throw new InvalidTransactionException(
                    "Idempotency key must not exceed 64 characters");
        }

        if (request.getSenderWalletId() == null
                || request.getReceiverWalletId() == null) {
            throw new InvalidTransactionException(
                    "Sender and receiver wallet IDs are required");
        }

        validateAmount(request.getAmount());

        String requestHash = generateRequestHash(request);

        Long senderId = request.getSenderWalletId();
        Long receiverId = request.getReceiverWalletId();


        if (senderId.equals(receiverId)) {
            throw new InvalidTransactionException(
                    "Cannot transfer to the same wallet");
        }

        UserEntity user = authService.getAuthenticatedUser();


        Optional<IdempotencyEntity> existing =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {

            IdempotencyEntity savedKey = existing.get();

            if (!savedKey.getUserId().equals(user.getUserId())) {
                throw new InvalidTransactionException(
                        "Idempotency key belongs to another user");
            }

            if (savedKey.getRequestHash() == null) {
                throw new InvalidTransactionException(
                        "Existing idempotency record has no request hash");
            }

            if (!savedKey.getRequestHash().equals(requestHash)) {
                throw new InvalidTransactionException(
                        "Idempotency key was already used for a different transfer");
            }

            TransactionEntity existingTransaction =
                    transactionRepository.findById(savedKey.getTransactionId())
                            .orElseThrow(() ->
                                    new InvalidTransactionException(
                                            "Original transaction not found"));

            return transactionMapper.toDTO(existingTransaction);
        }


        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);

        WalletEntity firstWallet = walletRepository.findWalletForUpdate(firstId)
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        WalletEntity secondWallet = walletRepository.findWalletForUpdate(secondId)
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        WalletEntity sender = senderId.equals(firstId)
                ? firstWallet
                : secondWallet;

        WalletEntity receiver = receiverId.equals(firstId)
                ? firstWallet
                : secondWallet;


        if (!sender.getUser().getUserId().equals(user.getUserId())) {
            throw new InvalidTransactionException(
                    "Sender wallet does not belong to authenticated user");
        }

        if (fraudService.isSuspicious(senderId)) {

            throw new InvalidTransactionException(
                    "Transaction flagged for fraud review");
        }

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        sender.setBalance(
                sender.getBalance().subtract(request.getAmount())
        );

        receiver.setBalance(
                receiver.getBalance().add(request.getAmount())
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransactionEntity transaction = new TransactionEntity();

        transaction.setSenderWallet(sender);
        transaction.setReceiverWallet(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        TransactionEntity savedTransaction =
                transactionRepository.save(transaction);
        IdempotencyEntity idempotency =
                new IdempotencyEntity();

        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setUserId(user.getUserId());
        idempotency.setTransactionId(
                savedTransaction.getTransactionId());
        idempotency.setCreatedAt(LocalDateTime.now());

        idempotency.setRequestHash(requestHash);

        idempotencyRepository.save(idempotency);

        auditLogService.log(
                user.getUserId(),
                "TRANSFER",
                "Transferred " + request.getAmount() +
                        " from wallet " + request.getSenderWalletId() +
                        " to wallet " + request.getReceiverWalletId()
        );

        return transactionMapper.toDTO(savedTransaction);
    }

    private String generateRequestHash(TransferRequestDTO request) {
        String transferDetails =
                request.getSenderWalletId() + ":" +
                        request.getReceiverWalletId() + ":" +
                        request.getAmount()
                                .stripTrailingZeros()
                                .toPlainString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    transferDetails.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Unable to generate transfer request hash", e
            );
        }
    }


    public Page<TransferResponseDTO> getWalletTransactions(
            Pageable pageable) {

        UserEntity user = authService.getAuthenticatedUser();

        WalletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        Page<TransactionEntity> transactions =
                transactionRepository
                        .findBySenderWallet_WalletIdOrReceiverWallet_WalletId(
                                wallet.getWalletId(),
                                wallet.getWalletId(),
                                pageable);

        return transactions.map(transactionMapper::toDTO);
    }

    public TransferResponseDTO getTransactionById(Long transactionId) {

        UserEntity user = authService.getAuthenticatedUser();

        WalletEntity userWallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        TransactionEntity transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found"));

        boolean isSender =
                transaction.getSenderWallet() != null &&
                        transaction.getSenderWallet().getWalletId()
                                .equals(userWallet.getWalletId());

        boolean isReceiver =
                transaction.getReceiverWallet() != null &&
                        transaction.getReceiverWallet().getWalletId()
                                .equals(userWallet.getWalletId());

        if (!isSender && !isReceiver) {
            throw new InvalidTransactionException(
                    "Transaction does not belong to authenticated user");
        }

        return transactionMapper.toDTO(transaction);
    }




    @Transactional
    public TransferResponseDTO withdraw(
            BigDecimal amount,
            String idempotencyKey) {

        validateAmount(amount);
        validateIdempotencyKey(idempotencyKey);

        UserEntity user = authService.getAuthenticatedUser();

        String requestHash = generateWithdrawalRequestHash(
                user.getUserId(), amount);

        Optional<TransferResponseDTO> existing =
                findExistingIdempotentTransaction(
                        idempotencyKey, user.getUserId(), requestHash);

        if (existing.isPresent()) {
            return existing.get();
        }

        WalletEntity wallet = walletRepository
                .findWalletForUpdateByUserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));


        existing = findExistingIdempotentTransaction(
                idempotencyKey, user.getUserId(), requestHash);

        if (existing.isPresent()) {
            return existing.get();
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setSenderWallet(wallet);
        transaction.setReceiverWallet(null);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        TransactionEntity savedTransaction =
                transactionRepository.save(transaction);

        IdempotencyEntity idempotency = new IdempotencyEntity();
        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setUserId(user.getUserId());
        idempotency.setTransactionId(
                savedTransaction.getTransactionId());
        idempotency.setRequestHash(requestHash);
        idempotency.setCreatedAt(LocalDateTime.now());

        idempotencyRepository.save(idempotency);

        auditLogService.log(
                user.getUserId(),
                "WITHDRAW",
                "Withdrew " + amount
                        + " from wallet " + wallet.getWalletId()
        );

        return transactionMapper.toDTO(savedTransaction);
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidTransactionException(
                    "Idempotency-Key header is required");
        }

        if (idempotencyKey.length() > 64) {
            throw new InvalidTransactionException(
                    "Idempotency key must not exceed 64 characters");
        }
    }

    private String generateWithdrawalRequestHash(
            Long userId, BigDecimal amount) {

        String details = "WITHDRAW:" + userId + ":"
                + amount.stripTrailingZeros().toPlainString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(details.getBytes(StandardCharsets.UTF_8)));

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Unable to generate withdrawal request hash", e);
        }
    }

    private Optional<TransferResponseDTO> findExistingIdempotentTransaction(
            String idempotencyKey,
            Long userId,
            String requestHash) {

        Optional<IdempotencyEntity> existing =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyEntity savedKey = existing.get();

        if (!savedKey.getUserId().equals(userId)) {
            throw new InvalidTransactionException(
                    "Idempotency key belongs to another user");
        }

        if (savedKey.getRequestHash() == null
                || !savedKey.getRequestHash().equals(requestHash)) {
            throw new InvalidTransactionException(
                    "Idempotency key was already used for a different request");
        }

        TransactionEntity transaction = transactionRepository
                .findById(savedKey.getTransactionId())
                .orElseThrow(() ->
                        new InvalidTransactionException(
                                "Original transaction not found"));

        return Optional.of(transactionMapper.toDTO(transaction));
    }


    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Amount must be greater than zero");
        }

        if (amount.compareTo(new BigDecimal("50000.00")) > 0) {
            throw new InvalidTransactionException(
                    "Amount cannot exceed 50000.00");
        }

        if (amount.scale() > 2) {
            throw new InvalidTransactionException(
                    "Amount cannot have more than two decimal places");
        }
    }




}

