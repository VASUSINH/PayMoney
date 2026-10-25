
package com.PayMoney.Controller;

import com.PayMoney.DTO.PaymentRequestDTO;
import com.PayMoney.DTO.PaymentResponseDTO;
import com.PayMoney.DTO.PaymentLinkVerificationRequestDTO;
import com.PayMoney.Service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/v1/payment-links")
    public ResponseEntity<PaymentResponseDTO> createPaymentLink(
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO response = paymentService.createPaymentLink(request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/v1/payment-link-verifications")
    public ResponseEntity<String> verifyPaymentLink(
            @Valid @RequestBody PaymentLinkVerificationRequestDTO request) {

        paymentService.verifyPaymentLink(request);

        return ResponseEntity.ok("Payment verified and wallet credited successfully");
    }

    @GetMapping("/api/v1/payment-links/callback")
    public ResponseEntity<String> paymentLinkCallback(
            @RequestParam("razorpay_payment_link_id")
            String paymentLinkId,

            @RequestParam("razorpay_payment_id")
            String paymentId,

            @RequestParam("razorpay_payment_link_reference_id")
            String referenceId,

            @RequestParam("razorpay_payment_link_status")
            String linkStatus,

            @RequestParam("razorpay_signature")
            String signature) {

        PaymentLinkVerificationRequestDTO request =
                new PaymentLinkVerificationRequestDTO();

        request.setRazorpayPaymentLinkId(paymentLinkId);
        request.setRazorpayPaymentId(paymentId);
        request.setRazorpayPaymentLinkReferenceId(referenceId);
        request.setRazorpayPaymentLinkStatus(linkStatus);
        request.setRazorpaySignature(signature);

        paymentService.verifyPaymentLinkCallback(request);

        return ResponseEntity.ok(
                "Payment processed. You can return to PayMoney."
        );
    }

}
