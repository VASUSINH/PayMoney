package com.PayMoney.Controller;

import com.PayMoney.DTO.refundRequestDTO;
import com.PayMoney.Service.paymentService;
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
public class refundController {

    @Autowired
    private paymentService paymentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/api/payments/refund")
    public ResponseEntity<String> refundPayment(
            @Valid @RequestBody refundRequestDTO request) {

        paymentService.refundPayment(request);

        return ResponseEntity.ok(
                "Payment refunded successfully");
    }
}