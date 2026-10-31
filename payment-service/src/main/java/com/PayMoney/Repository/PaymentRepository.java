
package com.PayMoney.Repository;

import com.PayMoney.Entity.PaymentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p FROM PaymentEntity p
            WHERE p.razorpayPaymentLinkId = :linkId
            """)
    Optional<PaymentEntity> findByRazorpayPaymentLinkIdForUpdate(
            @Param("linkId") String linkId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p FROM PaymentEntity p
            WHERE p.razorpayPaymentId = :paymentId
            """)
    Optional<PaymentEntity> findByRazorpayPaymentIdForUpdate(
            @Param("paymentId") String paymentId
    );

}
