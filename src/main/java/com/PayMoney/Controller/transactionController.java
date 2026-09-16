package com.PayMoney.Controller;

import com.PayMoney.DTO.transferRequestDTO;
import com.PayMoney.DTO.transferResponseDTO;
import com.PayMoney.Service.transactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class transactionController {

    @Autowired
    private transactionService transactionService;

    @PostMapping("/api/transactions/transfer")
    @PreAuthorize("hasRole('USER')")
    public transferResponseDTO transfer(
            @Valid @RequestBody transferRequestDTO request) {

        return transactionService.transfer(request);
    }

    @GetMapping("/api/transactions/wallet/{walletId}")
    @PreAuthorize("hasRole('USER')")
    public List<transferResponseDTO> getWalletTransactions(
            @PathVariable Long walletId) {

        return transactionService.getWalletTransactions(walletId);
    }

    @GetMapping("/api/transactions/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public transferResponseDTO getTransactionById(
            @PathVariable Long transactionId) {

        return transactionService.getTransactionById(transactionId);
    }
}