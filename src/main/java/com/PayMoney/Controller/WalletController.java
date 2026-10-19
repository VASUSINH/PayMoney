package com.PayMoney.Controller;


import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.DTO.WalletResponseDTO;
import com.PayMoney.DTO.WithdrawRequestDTO;
import com.PayMoney.Service.TransactionService;
import com.PayMoney.Service.WalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
@RestController
public class WalletController {
    @Autowired
    private WalletService walletService;

  @Autowired
  private TransactionService transactionService;
    @GetMapping("/api/wallet")
    @PreAuthorize("hasRole('USER')")
    public WalletResponseDTO getMyWallet() {
        return walletService.getMyWallet();
    }

    @PostMapping("/api/wallet/withdraw")
    @PreAuthorize("hasRole('USER')")
    public TransferResponseDTO withdraw(
            @Valid @RequestBody WithdrawRequestDTO request) {

        return transactionService.withdraw(request.getAmount());
    }
    }

