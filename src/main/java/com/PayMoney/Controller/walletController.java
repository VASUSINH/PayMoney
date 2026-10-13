package com.PayMoney.Controller;

import com.PayMoney.DTO.depositRequestDTO;
import com.PayMoney.DTO.transferResponseDTO;
import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.DTO.withdrawRequestDTO;
import com.PayMoney.Service.transactionService;
import com.PayMoney.Service.walletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
@RestController
public class walletController {
    @Autowired
    private walletService walletService;

  @Autowired
  private transactionService transactionService;
    @GetMapping("/api/wallet")
    @PreAuthorize("hasRole('USER')")
    public walletResponseDTO getMyWallet() {
        return walletService.getMyWallet();
    }

    @PostMapping("/api/wallet/deposit")
    @PreAuthorize("hasRole('USER')")
    public transferResponseDTO deposit(
            @Valid @RequestBody depositRequestDTO request) {

        return transactionService.deposit(request.getAmount());
    }

    @PostMapping("/api/wallet/withdraw")
    @PreAuthorize("hasRole('USER')")
    public transferResponseDTO withdraw(
            @Valid @RequestBody withdrawRequestDTO request) {

        return transactionService.withdraw(request.getAmount());
    }
    }

