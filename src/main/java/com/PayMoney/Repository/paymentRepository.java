package com.PayMoney.Repository;

import com.PayMoney.Entity.paymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface paymentRepository
        extends JpaRepository<paymentEntity, Long> {

    Optional<paymentEntity> findByRazorpayOrderId(String razorpayOrderId);

}