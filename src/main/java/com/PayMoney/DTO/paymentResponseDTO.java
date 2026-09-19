package com.PayMoney.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class paymentResponseDTO {

    private String orderId;
    private BigDecimal amount;
    private String currency;
}
