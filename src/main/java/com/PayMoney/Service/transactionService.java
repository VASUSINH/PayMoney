package com.PayMoney.Service;

import com.PayMoney.DTO.transactionRequestDTO;
import com.PayMoney.DTO.transactionResponseDTO;
import com.PayMoney.Entity.transactionEntity;
import com.PayMoney.Entity.transactionStatus;
import com.PayMoney.Entity.transactionType;
import com.PayMoney.Entity.walletEntity;
import com.PayMoney.Exception.insufficientBalanceException;
import com.PayMoney.Exception.invalidTransactionException;
import com.PayMoney.Exception.transactionNotFoundException;
import com.PayMoney.Exception.walletNotFoundException;
import com.PayMoney.Repository.transactionRepository;
import com.PayMoney.Repository.walletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class transactionService {

    @Autowired
    private walletRepository walletRepository;

    @Autowired
    private transactionRepository transactionRepository;

    @Transactional
    public transactionResponseDTO transfer(transactionRequestDTO request) {

        // Get sender wallet
        walletEntity sender = walletRepository.findById(request.getSenderWalletId())
                .orElseThrow(() ->
                        new walletNotFoundException("Sender wallet not found"));

        // Get receiver wallet
        walletEntity receiver = walletRepository.findById(request.getReceiverWalletId())
                .orElseThrow(() ->
                        new walletNotFoundException("Receiver wallet not found"));

        // Don't allow transferring to yourself
        if (sender.getWalletId().equals(receiver.getWalletId())) {
            throw new invalidTransactionException("Cannot transfer to the same wallet");
        }

        // Check sender balance
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new insufficientBalanceException("Insufficient wallet balance");
        }

        // Deduct from sender
        sender.setBalance(
                sender.getBalance().subtract(request.getAmount())
        );

        // Add to receiver
        receiver.setBalance(
                receiver.getBalance().add(request.getAmount())
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        // Create transaction record
        transactionEntity transaction = new transactionEntity();

        transaction.setSenderWallet(sender);
        transaction.setReceiverWallet(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setType(transactionType.TRANSFER);
        transaction.setStatus(transactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionEntity savedTransaction =
                transactionRepository.save(transaction);

        return new transactionResponseDTO(
                savedTransaction.getTransactionId(),
                sender.getWalletId(),
                receiver.getWalletId(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getStatus(),
                savedTransaction.getCreatedAt()
        );
    }

    public List<transactionResponseDTO> getWalletTransactions(Long walletId) {

        List<transactionEntity> transactions =
                transactionRepository
                        .findBySenderWallet_WalletIdOrReceiverWallet_WalletId(walletId, walletId);

        return transactions.stream()
                .map(transaction -> new transactionResponseDTO(
                        transaction.getTransactionId(),
                        transaction.getSenderWallet().getWalletId(),
                        transaction.getReceiverWallet().getWalletId(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getStatus(),
                        transaction.getCreatedAt()
                ))
                .toList();
    }

    public transactionResponseDTO getTransactionById(Long transactionId) {

        transactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new transactionNotFoundException("Transaction not found"));

        return new transactionResponseDTO(
                transaction.getTransactionId(),
                transaction.getSenderWallet().getWalletId(),
                transaction.getReceiverWallet().getWalletId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
