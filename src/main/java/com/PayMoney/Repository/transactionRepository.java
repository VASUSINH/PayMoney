package com.PayMoney.Repository;

import com.PayMoney.Entity.transactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
    public interface transactionRepository
            extends JpaRepository<transactionEntity, Long> {

        List<transactionEntity> findBySenderWallet_WalletIdOrReceiverWallet_WalletId(
                Long senderWalletId,
                Long receiverWalletId
        );

    long countBySenderWallet_WalletIdAndCreatedAtAfter(
            Long walletId,
            LocalDateTime time
    );
    }

