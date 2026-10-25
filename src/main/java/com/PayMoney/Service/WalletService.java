
package com.PayMoney.Service;

import com.PayMoney.DTO.TransferRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.DTO.WalletResponseDTO;
import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Entity.WalletEntity;
import com.PayMoney.Exception.WalletNotFoundException;
import com.PayMoney.Mapper.walletMapper;
import com.PayMoney.Repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private walletMapper walletMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionService transactionService;

    @Cacheable(
            cacheNames = "walletByUserId",
            key = "@authService.getAuthenticatedUser().getUserId()")
    public WalletResponseDTO getMyWallet() {

        UserEntity user = authService.getAuthenticatedUser();

        WalletEntity wallet = walletRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        return walletMapper.toDTO(wallet);
    }

    @CacheEvict(cacheNames = "walletByUserId", allEntries = true)
    public TransferResponseDTO transfer(
            TransferRequestDTO request,
            String idempotencyKey) {

        return transactionService.transfer(request, idempotencyKey);
    }

    @CacheEvict(cacheNames = "walletByUserId", allEntries = true)
    public TransferResponseDTO withdraw(
            BigDecimal amount, String idempotencyKey) {

        return transactionService.withdraw(amount, idempotencyKey);
    }
}
