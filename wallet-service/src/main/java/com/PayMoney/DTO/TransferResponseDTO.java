package com.PayMoney.DTO;

import com.PayMoney.Entity.TransactionStatus;
import com.PayMoney.Entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
    public class TransferResponseDTO {

        private Long transactionId;
        private Long senderWalletId;
        private Long receiverWalletId;
        private BigDecimal amount;
        private TransactionType type;
        private TransactionStatus status;
        private LocalDateTime createdAt;
    }

