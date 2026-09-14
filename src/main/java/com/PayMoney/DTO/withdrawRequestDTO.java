package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class withdrawRequestDTO {
    @NonNull
    @DecimalMin(value="0.01")
    private BigDecimal amount;
}
