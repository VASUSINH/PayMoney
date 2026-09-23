package com.PayMoney.Service;

import com.PayMoney.DTO.paymentRequestDTO;
import com.PayMoney.DTO.paymentResponseDTO;
import com.PayMoney.DTO.paymentVerificationRequestDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.externalServiceException;
import com.PayMoney.Exception.walletNotFoundException;
import com.PayMoney.Repository.paymentRepository;
import com.PayMoney.Repository.transactionRepository;
import com.PayMoney.Repository.walletRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private walletRepository walletRepository;

    @Autowired
    private transactionRepository transactionRepository;

    @Value("${razorpay.key.secret}")
    private String keySecret;

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

            throw new externalServiceException(
                    "Payment gateway request failed", e);
        }
    }

    @Transactional
    public boolean verifyPayment(
            paymentVerificationRequestDTO request) {

        paymentEntity payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                request.getRazorpayOrderId())
                        .orElseThrow(() ->
                                new RuntimeException("Payment not found"));

        userEntity user =
                authService.getAuthenticatedUser();

        if (!payment.getUser().getUserId()
                .equals(user.getUserId())) {

            throw new RuntimeException(
                    "Payment does not belong to authenticated user");
        }

        if (payment.getStatus() == paymentStatus.SUCCESS) {
            return true;
        }

        try {

            JSONObject options = new JSONObject();

            options.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId()
            );

            options.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            options.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );

            boolean verified =
                    Utils.verifyPaymentSignature(
                            options,
                            keySecret
                    );

            if (!verified) {
                return false;
            }

            walletEntity wallet =
                    walletRepository
                            .findByUser_UserId(user.getUserId())
                            .orElseThrow(() ->
                                    new walletNotFoundException(
                                            "Wallet not found"));

            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());

            payment.setStatus(paymentStatus.SUCCESS);

            paymentRepository.save(payment);

            wallet.setBalance(
                    wallet.getBalance().add(payment.getAmount()));

            walletRepository.save(wallet);

            transactionEntity transaction =
                    new transactionEntity();

            transaction.setSenderWallet(null);
            transaction.setReceiverWallet(wallet);
            transaction.setAmount(payment.getAmount());
            transaction.setType(transactionType.DEPOSIT);
            transaction.setStatus(transactionStatus.SUCCESS);
            transaction.setCreatedAt(LocalDateTime.now());

            transactionRepository.save(transaction);

            return true;

        } catch (Exception e) {
            throw new externalServiceException(
                    "Payment verification failed", e);
        }
    }


}