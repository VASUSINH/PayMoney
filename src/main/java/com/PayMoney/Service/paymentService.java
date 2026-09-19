package com.PayMoney.Service;

import com.PayMoney.DTO.paymentRequestDTO;
import com.PayMoney.DTO.paymentResponseDTO;
import com.PayMoney.Entity.paymentEntity;
import com.PayMoney.Entity.paymentStatus;
import com.PayMoney.Entity.userEntity;
import com.PayMoney.Repository.paymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class paymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private paymentRepository paymentRepository;

    @Autowired
    private authService authService;

    public paymentResponseDTO createOrder(paymentRequestDTO request) {

        userEntity user = authService.getAuthenticatedUser();

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Minimum payment amount is ₹1");
        }

        int amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .intValueExact();

        try {

            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "PAY_" + System.currentTimeMillis());

            Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);

            String razorpayOrderId =
                    razorpayOrder.get("id");

            paymentEntity payment = new paymentEntity();

            payment.setUser(user);
            payment.setAmount(amount);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setStatus(paymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            return new paymentResponseDTO(
                    razorpayOrderId,
                    amount,
                    "INR"
            );

        } catch (RazorpayException e) {

            throw new RuntimeException(
                    "Failed to create Razorpay order", e);
        }
    }
}