package com.PayMoney.Controller;

import com.PayMoney.DTO.depositRequestDTO;
import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.DTO.withdrawRequestDTO;
import com.PayMoney.Service.walletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class walletController {
    //Finds the Wallet for the User
    @Autowired
        private walletService walletService;
    @GetMapping("/api/wallet/{userId}")
    public walletResponseDTO getWallet(@PathVariable Long userId) {

        return walletService.getWalletByUserId(userId);
    }

    //Deposit the Amount.
    @PostMapping("/api/wallet/{userId}/deposit")
    public walletResponseDTO deposit(@PathVariable Long userId,
                                     @Valid @RequestBody depositRequestDTO request) {

        return walletService.deposit(userId, request.getAmount());
    }

    //Withdraw the Amount
    @PostMapping("/api/wallet/{userId}/withdraw")
    public walletResponseDTO withdraw(
            @PathVariable Long userId,
            @Valid @RequestBody withdrawRequestDTO request) {

        return walletService.withdraw(userId, request.getAmount());
    }
    }

