package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class transferRequestDTO {
        @NotNull
        private Long senderWalletId;

        @NotNull
        private Long receiverWalletId;

        @NotNull
        @DecimalMin(value = "1.0")
        private BigDecimal amount;
    }

