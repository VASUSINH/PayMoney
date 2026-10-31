
package com.PayMoney.Service;

import com.PayMoney.Entity.TransactionStatus;
import com.PayMoney.Entity.TransactionType;
import com.PayMoney.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FraudService {

    private static final int MAX_TRANSACTIONS_PER_MINUTE = 5;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public boolean isSuspicious(Long senderWalletId) {

        if (senderWalletId == null) {
            throw new IllegalArgumentException(
                    "Sender wallet ID is required");
        }

        LocalDateTime oneMinuteAgo =
                LocalDateTime.now().minusMinutes(1);

        long recentTransfers =
                transactionRepository
                        .countBySenderWallet_WalletIdAndTypeAndStatusAndCreatedAtAfter(
                                senderWalletId,
                                TransactionType.TRANSFER,
                                TransactionStatus.SUCCESS,
                                oneMinuteAgo);

        return recentTransfers >= MAX_TRANSACTIONS_PER_MINUTE;
    }
}
