package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class paymentRequestDTO {

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal amount;
}
