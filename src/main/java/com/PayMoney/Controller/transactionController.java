package com.PayMoney.Controller;

import com.PayMoney.DTO.transactionRequestDTO;
import com.PayMoney.DTO.transactionResponseDTO;
import com.PayMoney.Service.transactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class transactionController {

    @Autowired
    private transactionService transactionService;

    @PostMapping("/api/transactions/transfer")
    public transactionResponseDTO transfer(
            @Valid @RequestBody transactionRequestDTO request) {

        return transactionService.transfer(request);
    }

    @GetMapping("/api/transactions/wallet/{walletId}")
    public List<transactionResponseDTO> getWalletTransactions(
            @PathVariable Long walletId) {

        return transactionService.getWalletTransactions(walletId);
    }

    @GetMapping("/api/transactions/{transactionId}")
    public transactionResponseDTO getTransactionById(
            @PathVariable Long transactionId) {

        return transactionService.getTransactionById(transactionId);
    }
}