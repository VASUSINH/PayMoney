
package com.PayMoney.Service;

import com.PayMoney.DTO.PaymentDepositRequestDTO;
import com.PayMoney.DTO.TransferRequestDTO;
import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.DTO.UserAuthResponseDTO;
import com.PayMoney.DTO.UserRegisteredEventDTO;
import com.PayMoney.DTO.UserUpdatedEventDTO;
import com.PayMoney.DTO.WalletResponseDTO;
import com.PayMoney.Entity.WalletEntity;
import com.PayMoney.Exception.WalletNotFoundException;
import com.PayMoney.Mapper.walletMapper;
import com.PayMoney.Repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        UserAuthResponseDTO user = authService.getAuthenticatedUser();

        WalletEntity wallet = walletRepository
                .findByUserId(user.getUserId())
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

    @CacheEvict(cacheNames = "walletByUserId", allEntries = true)
    public TransferResponseDTO deposit(
            PaymentDepositRequestDTO request) {

        return transactionService.deposit(request);
    }

    // Moved from UserService.register(): runs when a user-registered event arrives.
    @Transactional
    public void createWallet(UserRegisteredEventDTO event) {

        if (walletRepository.findByUserId(event.getUserId()).isPresent()) {
            return;
        }

        WalletEntity wallet = new WalletEntity();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUserId(event.getUserId());
        wallet.setName(event.getName());

        walletRepository.save(wallet);
    }

    // Moved from UserService.updateUser(): runs when a user-updated event arrives.
    // The cached wallet contains the name, so the cache is cleared too.
    @CacheEvict(cacheNames = "walletByUserId", allEntries = true)
    @Transactional
    public void updateWalletName(UserUpdatedEventDTO event) {

        WalletEntity wallet = walletRepository
                .findByUserId(event.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        wallet.setName(event.getName());

        walletRepository.save(wallet);
    }
}
