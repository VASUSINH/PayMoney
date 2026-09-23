package com.PayMoney.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class refundRequestDTO {

    @NotBlank
    private String razorpayPaymentId;
}