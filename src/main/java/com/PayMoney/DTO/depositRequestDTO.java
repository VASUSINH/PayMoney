package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class depositRequestDTO {


        @NotNull
        @DecimalMin(value = "0.01")
        @DecimalMax("50000.00")
        private BigDecimal amount;
    }

