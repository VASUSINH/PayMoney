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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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

    public List<TransferResponseDTO> getWalletTransactions(Long walletId) {

        UserEntity user = authService.getAuthenticatedUser();

        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        if (!wallet.getUser().getUserId().equals(user.getUserId())) {
            throw new InvalidTransactionException(
                    "Wallet does not belong to authenticated user");
        }

        List<TransactionEntity> transactions =
                transactionRepository
                        .findBySenderWallet_WalletIdOrReceiverWallet_WalletId(
                                walletId, walletId);

        return transactions.stream()
                .map(transactionMapper::toDTO)
                .toList();
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

    public TransferResponseDTO withdraw(BigDecimal amount) {
        validateAmount(amount);
        UserEntity user = authService.getAuthenticatedUser();

        WalletEntity wallet =walletRepository
                .findWalletForUpdateByUserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient wallet balance");
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

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

        auditLogService.log(
                user.getUserId(),
                "WITHDRAW",
                "Withdrew " + amount +
                        " from wallet " + wallet.getWalletId()
        );

        return transactionMapper.toDTO(savedTransaction);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.compareTo(new BigDecimal("50000.00")) > 0
                || amount.stripTrailingZeros().scale() > 2) {

            throw new InvalidTransactionException(
                    "Amount must be positive, at most 50000, "
                            + "and have at most 2 decimal places"
            );
        }
    }

}

