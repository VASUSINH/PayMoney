package com.PayMoney.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDepositRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
}
