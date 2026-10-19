package com.PayMoney.Service;

import com.PayMoney.DTO.WalletResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.WalletNotFoundException;
import com.PayMoney.Mapper.walletMapper;
import com.PayMoney.Repository.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private walletMapper walletMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuditLogService auditLogService;



    public WalletResponseDTO getMyWallet() {

        UserEntity user = authService.getAuthenticatedUser();

        WalletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        return walletMapper.toDTO(wallet);
    }
}