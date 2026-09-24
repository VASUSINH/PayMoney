package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class withdrawRequestDTO {
    @NonNull
    @DecimalMin(value="1.0")
    @DecimalMax("50000.00")
    private BigDecimal amount;
}
