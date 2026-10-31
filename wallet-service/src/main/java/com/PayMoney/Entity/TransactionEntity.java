package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.FetchType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long transactionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_wallet_id",nullable=true)
        private WalletEntity senderWallet;


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_wallet_id",nullable= true)
        private WalletEntity receiverWallet;

        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal amount;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionType type;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionStatus status;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "payment_id")
        private Long paymentId;

    }

