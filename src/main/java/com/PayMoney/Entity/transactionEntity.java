package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class transactionEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long transactionId;

        @ManyToOne
        @JoinColumn(name = "sender_wallet_id",nullable=true)
        private walletEntity senderWallet;

        @ManyToOne
        @JoinColumn(name = "receiver_wallet_id",nullable= true)
        private walletEntity receiverWallet;

        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal amount;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private transactionType type;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private transactionStatus status;

        @Column(nullable = false)
        private LocalDateTime createdAt;
    }

