package com.PayMoney.Service;

import com.PayMoney.DTO.transferRequestDTO;
import com.PayMoney.DTO.transferResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.insufficientBalanceException;
import com.PayMoney.Exception.invalidTransactionException;
import com.PayMoney.Exception.transactionNotFoundException;
import com.PayMoney.Exception.walletNotFoundException;
import com.PayMoney.Mapper.transactionMapper;
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

    @Autowired
    private transactionMapper transactionMapper;

    @Autowired
    private authService authService;

    @Transactional
    public transferResponseDTO transfer(transferRequestDTO request) {

        // Get sender wallet
        Long senderId = request.getSenderWalletId();
        Long receiverId = request.getReceiverWalletId();

        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);

        walletEntity firstWallet = walletRepository.findWalletForUpdate(firstId)
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        walletEntity secondWallet = walletRepository.findWalletForUpdate(secondId)
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        walletEntity sender = senderId.equals(firstId)
                ? firstWallet
                : secondWallet;

        walletEntity receiver = receiverId.equals(firstId)
                ? firstWallet
                : secondWallet;

        userEntity user = authService.getAuthenticatedUser();

        if (!sender.getUser().getUserId().equals(user.getUserId())) {
            throw new invalidTransactionException(
                    "Sender wallet does not belong to authenticated user");
        }

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

        return transactionMapper.toDTO(savedTransaction);
    }

    //Get Wallet transaction Details
    public List<transferResponseDTO> getWalletTransactions(Long walletId) {

        userEntity user = authService.getAuthenticatedUser();

        walletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        if (!wallet.getUser().getUserId().equals(user.getUserId())) {
            throw new invalidTransactionException(
                    "Wallet does not belong to authenticated user");
        }

        List<transactionEntity> transactions =
                transactionRepository
                        .findBySenderWallet_WalletIdOrReceiverWallet_WalletId(
                                walletId, walletId);

        return transactions.stream()
                .map(transactionMapper::toDTO)
                .toList();
    }

    // Get Transaction By Transaction ID
    public transferResponseDTO getTransactionById(Long transactionId) {

        userEntity user = authService.getAuthenticatedUser();

        walletEntity userWallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        transactionEntity transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new transactionNotFoundException(
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
            throw new invalidTransactionException(
                    "Transaction does not belong to authenticated user");
        }

        return transactionMapper.toDTO(transaction);
    }

}

