
package com.PayMoney.Repository;

import com.PayMoney.Entity.TransactionEntity;
import com.PayMoney.Entity.TransactionStatus;
import com.PayMoney.Entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository
        extends JpaRepository<TransactionEntity, Long> {

    Page<TransactionEntity> findBySenderWallet_WalletIdOrReceiverWallet_WalletId(
            Long senderWalletId,
            Long receiverWalletId,
            Pageable pageable
    );

    long countBySenderWallet_WalletIdAndTypeAndStatusAndCreatedAtAfter(
            Long walletId,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime time
    );

    Optional<TransactionEntity> findFirstByPayment_PaymentIdAndTypeOrderByTransactionIdDesc(
            Long paymentId,
            TransactionType type
    );
}
