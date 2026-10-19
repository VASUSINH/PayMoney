package com.PayMoney.Repository;

import com.PayMoney.Entity.TransactionEntity;
import com.PayMoney.Entity.TransactionStatus;
import com.PayMoney.Entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
    public interface TransactionRepository
            extends JpaRepository<TransactionEntity, Long> {

        List<TransactionEntity> findBySenderWallet_WalletIdOrReceiverWallet_WalletId(
                Long senderWalletId,
                Long receiverWalletId
        );


    long countBySenderWallet_WalletIdAndTypeAndStatusAndCreatedAtAfter(
            Long walletId,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime time
    );

}

