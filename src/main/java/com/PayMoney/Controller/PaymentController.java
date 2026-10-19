package com.PayMoney.Controller;

import com.PayMoney.DTO.PaymentRequestDTO;
import com.PayMoney.DTO.PaymentResponseDTO;
import com.PayMoney.DTO.PaymentVerificationRequestDTO;
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
    @PostMapping("/api/payments/createOrder")
    public ResponseEntity<PaymentResponseDTO> createOrder(@Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO response =
                paymentService.createOrder(request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/payments/verify")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequestDTO request) {

        boolean verified =
                paymentService.verifyPayment(request);

        if (!verified) {
            return ResponseEntity
                    .badRequest()
                    .body("Payment verification failed");
        }

        return ResponseEntity.ok(
                "Payment verified successfully");
    }
}
