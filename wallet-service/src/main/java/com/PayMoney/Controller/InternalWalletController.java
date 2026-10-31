package com.PayMoney.Controller;

import com.PayMoney.DTO.PaymentDepositRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.Service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Called only by payment-service (not routed by the API Gateway).
// Callers must send the shared X-Internal-Key header.
@RestController
public class InternalWalletController {

    @Autowired
    private WalletService walletService;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @PostMapping("/api/v1/internal/wallets/deposits")
    public TransferResponseDTO deposit(
            @Valid @RequestBody PaymentDepositRequestDTO request,
            @RequestHeader(value = "X-Internal-Key", required = false)
            String internalKey) {

        if (!internalApiKey.equals(internalKey)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Invalid internal key");
        }

        return walletService.deposit(request);
    }
}
