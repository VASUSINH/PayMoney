package com.PayMoney.Controller;

import com.PayMoney.DTO.paymentRequestDTO;
import com.PayMoney.DTO.paymentResponseDTO;
import com.PayMoney.Service.paymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class paymentController {

    @Autowired
    private paymentService paymentService;

    @PostMapping("/api/payments/create-order")
    public ResponseEntity<paymentResponseDTO> createOrder(
            @Valid @RequestBody paymentRequestDTO request) {

        paymentResponseDTO response =
                paymentService.createOrder(request);

        return ResponseEntity.ok(response);
    }
}
