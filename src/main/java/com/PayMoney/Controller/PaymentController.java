
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearer-key")
@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/payments/createLink")
    public ResponseEntity<PaymentResponseDTO> createPaymentLink(
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO response = paymentService.createPaymentLink(request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/payments/verifyLink")
    public ResponseEntity<String> verifyPaymentLink(
            @Valid @RequestBody PaymentLinkVerificationRequestDTO request) {

        paymentService.verifyPaymentLink(request);

        return ResponseEntity.ok("Payment verified and wallet credited successfully");
    }
}
