package com.PayMoney.Service;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Entity.transactionEntity;
import com.PayMoney.Entity.transactionStatus;
import com.PayMoney.Entity.transactionType;
import com.PayMoney.Entity.walletEntity;
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

    //Fetch the wallet by user_id
    public walletResponseDTO getWalletByUserId(Long userId) {


        walletEntity wallet = walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return walletMapper.toDTO(wallet);
    }

    //Deposit Method.

    @Transactional
    public walletResponseDTO deposit(Long userId, BigDecimal amount) {

        walletEntity wallet = walletRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));

        walletRepository.save(wallet);

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
    public walletResponseDTO withdraw(Long userId, BigDecimal amount) {

        walletEntity wallet = walletRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        walletRepository.save(wallet);

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

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