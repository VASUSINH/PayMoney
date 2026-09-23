package com.PayMoney.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class paymentVerificationRequestDTO {

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpaySignature;
}