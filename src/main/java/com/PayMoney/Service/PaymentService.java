package com.PayMoney.Service;

import com.PayMoney.DTO.PaymentRequestDTO;
import com.PayMoney.DTO.PaymentResponseDTO;
import com.PayMoney.DTO.PaymentVerificationRequestDTO;
import com.PayMoney.DTO.RefundRequestDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.*;
import com.PayMoney.Repository.PaymentRepository;
import com.PayMoney.Repository.TransactionRepository;
import com.PayMoney.Repository.WalletRepository;
import com.razorpay.*;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentResponseDTO createOrder(PaymentRequestDTO request) {

        UserEntity user = authService.getAuthenticatedUser();

        BigDecimal amount = request.getAmount();

        if (amount == null
                || amount.compareTo(BigDecimal.ONE) < 0
                || amount.compareTo(new BigDecimal("50000.00")) > 0
                || amount.stripTrailingZeros().scale() > 2) {

            throw new InvalidTransactionException(
                    "Amount must be between ₹1 and ₹50,000 with at most 2 decimal places"
            );
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

            PaymentEntity payment = new PaymentEntity();

            payment.setUser(user);
            payment.setAmount(amount);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            auditLogService.log(
                    user.getUserId(),
                    "PAYMENT",
                    "Created Razorpay payment order " + razorpayOrderId +
                            " for amount " + amount
            );

            return new PaymentResponseDTO(
                    razorpayOrderId,
                    amount,
                    "INR"
            );

        }  catch (RazorpayException e) {
        log.error("Razorpay order creation failed | userId={} | amount={} | reason={}",
                user.getUserId(), amount, e.getMessage(), e);
        throw new ExternalServiceException(
                "Payment gateway request failed", e);
    }
    }


    @Transactional
    public boolean verifyPayment(PaymentVerificationRequestDTO request) {

        if (request == null) {
            throw new InvalidTransactionException(
                    "Payment verification request cannot be null");
        }

        PaymentEntity payment = paymentRepository
                .findByOrderIdForUpdate(request.getRazorpayOrderId())
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found"));

        UserEntity user = authService.getAuthenticatedUser();

        if (!payment.getUser().getUserId().equals(user.getUserId())) {
            throw new InvalidTransactionException(
                    "Payment does not belong to authenticated user");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (request.getRazorpayPaymentId()
                    .equals(payment.getRazorpayPaymentId())) {
                return true;
            }

            throw new InvalidTransactionException(
                    "Payment was already completed with a different payment ID");
        }

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new InvalidTransactionException(
                    "Payment is not in a verifiable state");
        }

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", request.getRazorpayOrderId());
        options.put("razorpay_payment_id", request.getRazorpayPaymentId());
        options.put("razorpay_signature", request.getRazorpaySignature());

        try {
            boolean verified =
                    Utils.verifyPaymentSignature(options, keySecret);

            if (!verified) {
                throw new InvalidTransactionException(
                        "Invalid payment signature");
            }
        } catch (RazorpayException e) {
            log.error("Razorpay signature verification failed | orderId={}",
                    request.getRazorpayOrderId(), e);

            throw new ExternalServiceException(
                    "Payment verification failed", e);
        }

        Payment razorpayPayment;

        try {
            razorpayPayment = razorpayClient.payments.fetch(
                    request.getRazorpayPaymentId());

        } catch (RazorpayException e) {
            log.error("Failed to fetch Razorpay payment | paymentId={}",
                    request.getRazorpayPaymentId(), e);

            throw new ExternalServiceException(
                    "Unable to verify payment status", e);
        }

        String actualOrderId = razorpayPayment.get("order_id");

        if (!request.getRazorpayOrderId().equals(actualOrderId)) {
            throw new InvalidTransactionException(
                    "Payment does not belong to the specified order");
        }

        String status = razorpayPayment.get("status");

        if (!"captured".equals(status)) {
            throw new InvalidTransactionException(
                    "Payment has not been captured yet");
        }

        Object amountValue = razorpayPayment.get("amount");

        if (!(amountValue instanceof Number)) {
            throw new InvalidTransactionException(
                    "Invalid payment amount returned by Razorpay");
        }

        int actualAmount = ((Number) amountValue).intValue();

        int expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValueExact();

        if (actualAmount != expectedAmount) {
            throw new InvalidTransactionException(
                    "Payment amount does not match the order amount");
        }

        String currency = razorpayPayment.get("currency");

        if (!"INR".equals(currency)) {
            throw new InvalidTransactionException(
                    "Invalid payment currency");
        }

        WalletEntity wallet = walletRepository
                .findWalletForUpdateByUserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        wallet.setBalance(wallet.getBalance().add(payment.getAmount()));
        walletRepository.save(wallet);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setSenderWallet(null);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(payment.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        auditLogService.log(
                user.getUserId(),
                "PAYMENT_VERIFIED",
                "Payment verified successfully for amount "
                        + payment.getAmount());

        return true;
    }

    @Transactional
    public void refundPayment(RefundRequestDTO request) {

        PaymentEntity payment = paymentRepository
                .findByRazorpayPaymentIdForUpdate(
                        request.getRazorpayPaymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found"));
        UserEntity user =
                authService.getAuthenticatedUser();

        if (!payment.getUser().getUserId()
                .equals(user.getUserId())) {

            throw new InvalidTransactionException(
                    "Payment does not belong to authenticated user");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new InvalidTransactionException(
                    "Payment has already been refunded");
        }


        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidTransactionException(
                    "Only successful payments can be refunded");
        }

        WalletEntity wallet = walletRepository
                .findWalletForUpdateByUserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet not found"));

        if (wallet.getBalance()
                .compareTo(payment.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient wallet balance for refund");
        }

        try {

            Refund refund =
                    razorpayClient.payments.refund(
                            request.getRazorpayPaymentId());

            payment.setRazorpayRefundId(
                    refund.get("id"));

            payment.setRefundedAmount(
                    payment.getAmount());

            payment.setStatus(PaymentStatus.REFUNDED);

            paymentRepository.save(payment);

            wallet.setBalance(
                    wallet.getBalance()
                            .subtract(payment.getAmount()));

            walletRepository.save(wallet);

            TransactionEntity transaction =
                    new TransactionEntity();

            transaction.setSenderWallet(wallet);
            transaction.setReceiverWallet(null);
            transaction.setAmount(payment.getAmount());
            transaction.setType(TransactionType.REFUND);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCreatedAt(LocalDateTime.now());

            transactionRepository.save(transaction);

            auditLogService.log(
                    payment.getUser().getUserId(),
                    "REFUND",
                    "Refunded payment of amount " + payment.getAmount()
            );

        } catch (RazorpayException e) {
        log.error("Razorpay refund failed | paymentId={} | amount={} | reason={}",
                request.getRazorpayPaymentId(),
                payment.getAmount(),
                e.getMessage(), e);
        throw new ExternalServiceException(
                "Refund request failed", e);
    }
    }


}