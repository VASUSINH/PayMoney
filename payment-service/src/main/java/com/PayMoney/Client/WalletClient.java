package com.PayMoney.Client;

import com.PayMoney.DTO.PaymentDepositRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/api/v1/internal/wallets/deposits")
    void deposit(@RequestBody PaymentDepositRequestDTO request);
}
