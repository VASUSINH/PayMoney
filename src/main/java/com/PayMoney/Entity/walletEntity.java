package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="wallets")
public class walletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="walletId")
    private Long walletId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false, unique = true)
    private userEntity user;
}
