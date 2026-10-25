
package com.PayMoney.Controller;

import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.Service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/api/v1/wallet/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<TransferResponseDTO>> getWalletTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "transactionId"))
        );

        return ResponseEntity.ok(
                transactionService.getWalletTransactions(pageable));
    }

    @GetMapping("/api/v1/transactions/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponseDTO> getTransactionById(
            @PathVariable Long transactionId) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(transactionId));
    }
}
