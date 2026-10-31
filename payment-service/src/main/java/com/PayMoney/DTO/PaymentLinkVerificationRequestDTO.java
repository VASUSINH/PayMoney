
package com.PayMoney.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentLinkVerificationRequestDTO {

    @NotBlank(message = "Payment Link ID is required")
    private String razorpayPaymentLinkId;

    @NotBlank(message = "Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Payment Link reference ID is required")
    private String razorpayPaymentLinkReferenceId;

    @NotBlank(message = "Payment Link status is required")
    private String razorpayPaymentLinkStatus;

    @NotBlank(message = "Payment signature is required")
    private String razorpaySignature;
}
