package com.PayMoney.Service;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.insufficientBalanceException;
import com.PayMoney.Exception.walletNotFoundException;
import com.PayMoney.Mapper.walletMapper;
import com.PayMoney.Repository.transactionRepository;
import com.PayMoney.Repository.walletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class walletService {

    @Autowired
    private walletRepository walletRepository;

    @Autowired
    private walletMapper walletMapper;

    @Autowired
    private transactionRepository transactionRepository;

    @Autowired
    private authService authService;

    //Fetch the wallet
    public walletResponseDTO getMyWallet() {

        userEntity user = authService.getAuthenticatedUser();

        walletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        return walletMapper.toDTO(wallet);
    }

    //Deposit Method.

    @Transactional
    public walletResponseDTO deposit(BigDecimal amount) {

        userEntity user = authService.getAuthenticatedUser();

        walletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );

        walletRepository.save(wallet);

        // Your existing DEPOSIT transaction creation
        transactionEntity transaction = new transactionEntity();

        transaction.setSenderWallet(null);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(transactionType.DEPOSIT);
        transaction.setStatus(transactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        return walletMapper.toDTO(wallet);
    }

    // Withdraw Method
    @Transactional
    public walletResponseDTO withdraw(BigDecimal amount) {

        userEntity user = authService.getAuthenticatedUser();

        walletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new insufficientBalanceException(
                    "Insufficient wallet balance");
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        walletRepository.save(wallet);

        // Your existing withdraw transaction creation

        transactionEntity transaction = new transactionEntity();

        transaction.setSenderWallet(wallet);
        transaction.setReceiverWallet(null);
        transaction.setAmount(amount);
        transaction.setType(transactionType.WITHDRAW);
        transaction.setStatus(transactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        return walletMapper.toDTO(wallet);
    }


}