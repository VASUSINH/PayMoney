package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = {
                @UniqueConstraint(columnNames = "idempotencyKey")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class idempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}