package com.PayMoney.Service;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.walletNotFoundException;
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

    @Autowired
    private authService authService;

    @Autowired
    private auditLogService auditLogService;

    //Fetch the wallet and view Balance
    public walletResponseDTO getMyWallet() {

        userEntity user = authService.getAuthenticatedUser();

        walletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new walletNotFoundException("Wallet not found"));

        return walletMapper.toDTO(wallet);
    }
}