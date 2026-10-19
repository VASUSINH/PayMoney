package com.PayMoney.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequestDTO {

    @NotBlank
    private String razorpayPaymentId;
}