package com.PayMoney.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class depositRequestDTO {


        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal amount;
    }

