package com.PayMoney.Service;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Entity.walletEntity;
import com.PayMoney.Mapper.walletMapper;
import com.PayMoney.Repository.walletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class walletService {

    @Autowired
    private walletRepository walletRepository;
    @Autowired
    private walletMapper walletMapper;

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

        return walletMapper.toDTO(wallet);
    }
}