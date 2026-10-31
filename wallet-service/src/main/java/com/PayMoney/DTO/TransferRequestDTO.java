package com.PayMoney.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {
        @NotNull(message = "Sender wallet ID is required")
        @Positive(message = "Sender wallet ID must be positive")
        private Long senderWalletId;

        @NotNull(message = "Receiver wallet ID is required")
        @Positive(message = "Receiver wallet ID must be positive")
        private Long receiverWalletId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00",
                message = "Amount must be at least 1.00")
        @DecimalMax(value = "50000.00",
                message = "Amount cannot exceed 50000")
        @Digits(integer = 17, fraction = 2,message = "Amount must have at most 2 decimal places")
        private BigDecimal amount;
    }

