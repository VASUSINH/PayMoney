package com.PayMoney.Controller;

import com.PayMoney.DTO.transferRequestDTO;
import com.PayMoney.DTO.transferResponseDTO;
import com.PayMoney.Service.transactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearer-key")
@RestController
public class transactionController {

    @Autowired
    private transactionService transactionService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/transactions/transfer")
    public ResponseEntity<transferResponseDTO> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody transferRequestDTO request) {

        transferResponseDTO response =
                transactionService.transfer(request, idempotencyKey);

        return ResponseEntity.ok(response);
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