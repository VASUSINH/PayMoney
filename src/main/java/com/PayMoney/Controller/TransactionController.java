package com.PayMoney.Controller;

import com.PayMoney.DTO.TransferRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.Service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearer-key")
@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/transactions/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(
            @Valid @RequestBody TransferRequestDTO request , @RequestHeader("Idempotency-Key") String idempotencyKey) {

        TransferResponseDTO response =
                transactionService.transfer(request, idempotencyKey);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/transactions/wallet/{walletId}")
    @PreAuthorize("hasRole('USER')")
    public List<TransferResponseDTO> getWalletTransactions(
           @Valid @PathVariable Long walletId) {

        return transactionService.getWalletTransactions(walletId);
    }

    @GetMapping("/api/transactions/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public TransferResponseDTO getTransactionById(
            @PathVariable Long transactionId) {

        return transactionService.getTransactionById(transactionId);
    }
}