package com.PayMoney.DTO;

import com.PayMoney.Entity.transactionStatus;
import com.PayMoney.Entity.transactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
    public class transactionResponseDTO {

        private Long transactionId;
        private Long senderWalletId;
        private Long receiverWalletId;
        private BigDecimal amount;
        private transactionType type;
        private transactionStatus status;
        private LocalDateTime createdAt;
    }

