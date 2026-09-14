package com.PayMoney.Service;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Entity.walletEntity;
import com.PayMoney.Mapper.walletMapper;
import com.PayMoney.Repository.walletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class walletService {

    @Autowired
    private walletRepository walletRepository;
    @Autowired
    private walletMapper walletMapper;
    public walletResponseDTO getWalletByUserId(Long userId) {


        walletEntity wallet = walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return walletMapper.toDTO(wallet);
    }
}