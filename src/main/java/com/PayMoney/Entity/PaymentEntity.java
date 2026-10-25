
package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "razorpay_payment_id", unique = true)
    private String razorpayPaymentId;

    @Column(name = "razorpay_payment_link_id", unique = true)
    private String razorpayPaymentLinkId;

    @Column(name = "razorpay_payment_link_reference_id", unique = true)
    private String razorpayPaymentLinkReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "wallet_credited", nullable = false)
    private boolean walletCredited = false;

}
