package com.PayMoney.Controller;

import com.PayMoney.DTO.refundRequestDTO;
import com.PayMoney.Service.paymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class refundController {

    @Autowired
    private paymentService paymentService;

    @PostMapping("/api/payments/refund")
    public ResponseEntity<String> refundPayment(
            @Valid @RequestBody refundRequestDTO request) {

        paymentService.refundPayment(request);

        return ResponseEntity.ok(
                "Payment refunded successfully");
    }
}