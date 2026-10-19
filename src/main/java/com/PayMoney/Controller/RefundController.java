package com.PayMoney.Controller;

import com.PayMoney.DTO.RefundRequestDTO;
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
public class RefundController {

    @Autowired
    private PaymentService paymentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/payments/refund")
    public ResponseEntity<String> refundPayment(
            @Valid @RequestBody RefundRequestDTO request) {

        paymentService.refundPayment(request);

        return ResponseEntity.ok(
                "Payment refunded successfully");
    }
}