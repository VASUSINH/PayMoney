package com.PayMoney.Controller;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Service.walletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class walletController {

    @Autowired
        private walletService walletService;
    @GetMapping("/api/wallet/{userId}")
    public walletResponseDTO getWallet(@PathVariable Long userId) {

        return walletService.getWalletByUserId(userId);
    }
    }

