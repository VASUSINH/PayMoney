package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "idempotency_key"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}