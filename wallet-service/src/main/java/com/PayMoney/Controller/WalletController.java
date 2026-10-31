
package com.PayMoney.Controller;

import com.PayMoney.DTO.TransferRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.DTO.WalletResponseDTO;
import com.PayMoney.DTO.WithdrawRequestDTO;
import com.PayMoney.Service.WalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/api/v1/wallet")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletResponseDTO> getMyWallet() {
        return ResponseEntity.ok(walletService.getMyWallet());
    }

    @PostMapping("/api/v1/transfers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponseDTO> transfer(
            @Valid @RequestBody TransferRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        TransferResponseDTO response = walletService.transfer(request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/withdrawals")
    @PreAuthorize("hasRole('USER')")
    public TransferResponseDTO withdraw(
            @Valid @RequestBody WithdrawRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return walletService.withdraw(
                request.getAmount(), idempotencyKey);
    }
}
