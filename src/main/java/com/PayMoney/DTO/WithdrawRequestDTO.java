package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class WithdrawRequestDTO {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00",
            message = "Amount must be at least 1.00")
    @DecimalMax(value = "50000.00",
            message = "Amount cannot exceed 50000")
    @Digits(integer = 17, fraction = 2,message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;
}
