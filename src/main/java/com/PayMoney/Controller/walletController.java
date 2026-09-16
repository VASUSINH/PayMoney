package com.PayMoney.Controller;

import com.PayMoney.DTO.depositRequestDTO;
import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.DTO.withdrawRequestDTO;
import com.PayMoney.Service.walletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class walletController {
    //Finds the Wallet for the User
    @Autowired
    private walletService walletService;


    @GetMapping("/api/wallet")
    @PreAuthorize("hasRole('USER')")
    public walletResponseDTO getMyWallet() {
        return walletService.getMyWallet();
    }

    //Deposit the Amount.
    @PostMapping("/api/wallet/deposit")
    @PreAuthorize("hasRole('USER')")
    public walletResponseDTO deposit(
            @Valid @RequestBody depositRequestDTO request) {

        return walletService.deposit(request.getAmount());
    }

    //Withdraw the Amount
    @PostMapping("/api/wallet/withdraw")
    @PreAuthorize("hasRole('USER')")
    public walletResponseDTO withdraw(
            @Valid @RequestBody withdrawRequestDTO request) {

        return walletService.withdraw(request.getAmount());
    }
    }

