package com.PayMoney.Service;

import com.PayMoney.Repository.transactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class fraudService {

    private static final BigDecimal MAX_TRANSACTION_AMOUNT =
            new BigDecimal("50000");

    private static final int MAX_TRANSACTIONS_PER_MINUTE = 5;

    @Autowired
    private transactionRepository transactionRepository;

    public boolean isSuspicious(
            Long senderWalletId,
            BigDecimal amount) {

        // Rule 1: Large transaction
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return true;
        }

        // Rule 2: Too many recent transfers
        LocalDateTime oneMinuteAgo =
                LocalDateTime.now().minusMinutes(1);

        long recentTransactions =
                transactionRepository
                        .countBySenderWallet_WalletIdAndCreatedAtAfter(
                                senderWalletId,
                                oneMinuteAgo);

        return recentTransactions >= MAX_TRANSACTIONS_PER_MINUTE;
    }
}