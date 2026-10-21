
package com.PayMoney.Service;

import com.PayMoney.DTO.PaymentLinkVerificationRequestDTO;
import com.PayMoney.DTO.PaymentRequestDTO;
import com.PayMoney.DTO.PaymentResponseDTO;
import com.PayMoney.DTO.RefundRequestDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.*;
import com.PayMoney.Repository.PaymentRepository;
import com.PayMoney.Repository.TransactionRepository;
import com.PayMoney.Repository.WalletRepository;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.razorpay.Utils;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

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

    public PaymentResponseDTO createPaymentLink(PaymentRequestDTO request) {

        if (request == null || request.getAmount() == null) {
            throw new InvalidTransactionException(
                    "Payment amount is required");
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(new BigDecimal("1.00")) < 0
                || amount.compareTo(new BigDecimal("50000.00")) > 0
                || amount.stripTrailingZeros().scale() > 2)  {
            throw new InvalidTransactionException(
                    "Amount must be between 1 and 50000 with at most two decimal places");
        }

        UserEntity user = authService.getAuthenticatedUser();

        int amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .intValueExact();

        String referenceId = UUID.randomUUID().toString();

        JSONObject customer = new JSONObject();
        customer.put("name", user.getName());
        customer.put("email", user.getEmail());

        JSONObject notify = new JSONObject();
        notify.put("email", false);
        notify.put("sms", false);

        JSONObject linkRequest = new JSONObject();
        linkRequest.put("amount", amountInPaise);
        linkRequest.put("currency", "INR");
        linkRequest.put("accept_partial", false);
        linkRequest.put("reference_id", referenceId);
        linkRequest.put("description", "PayMoney wallet top-up");
        linkRequest.put("customer", customer);
        linkRequest.put("notify", notify);
        linkRequest.put("reminder_enable", false);

        try {
            PaymentLink razorpayLink =
                    razorpayClient.paymentLink.create(linkRequest);

            String linkId = razorpayLink.get("id");
            String shortUrl = razorpayLink.get("short_url");

            if (linkId == null || linkId.isBlank()
                    || shortUrl == null || shortUrl.isBlank()) {
                throw new ExternalServiceException(
                        "Payment gateway returned an invalid Payment Link",
                        null);
            }

            PaymentEntity payment = new PaymentEntity();
            payment.setUser(user);
            payment.setAmount(amount);
            payment.setRazorpayPaymentLinkId(linkId);
            payment.setRazorpayPaymentLinkReferenceId(referenceId);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());

            paymentRepository.saveAndFlush(payment);

            auditLogService.log(
                    user.getUserId(),
                    "PAYMENT_LINK_CREATED",
                    "Created Payment Link " + linkId
                            + " for amount " + amount);

            return new PaymentResponseDTO(
                    linkId,
                    shortUrl,
                    amount,
                    "INR",
                    "created");

        } catch (RazorpayException e) {
            log.error(
                    "Razorpay Payment Link creation failed for userId={}",
                    user.getUserId(),
                    e);

            throw new ExternalServiceException(
                    "Unable to create Payment Link",
                    e);
        }
    }

    @Transactional
    public boolean verifyPaymentLink(
            PaymentLinkVerificationRequestDTO request) {

        if (request == null) {
            throw new InvalidTransactionException(
                    "Payment verification request is required");
        }

        if (!"paid".equalsIgnoreCase(
                request.getRazorpayPaymentLinkStatus())) {
            throw new InvalidTransactionException(
                    "Payment Link has not been paid");
        }

        UserEntity user = authService.getAuthenticatedUser();

        JSONObject signatureData = new JSONObject();
        signatureData.put(
                "payment_link_id",
                request.getRazorpayPaymentLinkId());
        signatureData.put(
                "payment_link_reference_id",
                request.getRazorpayPaymentLinkReferenceId());
        signatureData.put(
                "payment_link_status",
                request.getRazorpayPaymentLinkStatus());
        signatureData.put(
                "razorpay_payment_id",
                request.getRazorpayPaymentId());
        signatureData.put(
                "razorpay_signature",
                request.getRazorpaySignature());

        try {
            if (!Utils.verifyPaymentLink(signatureData, keySecret)) {
                throw new InvalidTransactionException(
                        "Invalid Payment Link signature");
            }
        } catch (RazorpayException e) {
            log.error("Payment Link signature verification failed", e);

            throw new ExternalServiceException(
                    "Unable to verify Payment Link signature",
                    e);
        }

        PaymentEntity payment = paymentRepository
                .findByRazorpayPaymentLinkIdForUpdate(
                        request.getRazorpayPaymentLinkId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment Link not found"));

        if (!payment.getUser().getUserId().equals(user.getUserId())) {
            throw new InvalidTransactionException(
                    "Payment Link does not belong to authenticated user");
        }

        if (!payment.getRazorpayPaymentLinkReferenceId().equals(
                request.getRazorpayPaymentLinkReferenceId())) {
            throw new InvalidTransactionException(
                    "Payment Link reference does not match");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (request.getRazorpayPaymentId().equals(
                    payment.getRazorpayPaymentId())) {
                return true;
            }

            throw new InvalidTransactionException(
                    "Payment Link has already been verified with another payment");
        }

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new InvalidTransactionException(
                    "Payment is not in a verifiable state");
        }

        try {
            PaymentLink razorpayLink =
                    razorpayClient.paymentLink.fetch(
                            request.getRazorpayPaymentLinkId());

            String gatewayLinkStatus = razorpayLink.get("status");
            String gatewayReferenceId = razorpayLink.get("reference_id");
            String gatewayCurrency = razorpayLink.get("currency");

            if (!"paid".equalsIgnoreCase(gatewayLinkStatus)) {
                throw new InvalidTransactionException(
                        "Razorpay has not marked this Payment Link as paid");
            }

            if (!request.getRazorpayPaymentLinkReferenceId()
                    .equals(gatewayReferenceId)) {
                throw new InvalidTransactionException(
                        "Gateway Payment Link reference does not match");
            }

            if (!"INR".equals(gatewayCurrency)) {
                throw new InvalidTransactionException(
                        "Payment currency is invalid");
            }

            Object linkAmountValue = razorpayLink.get("amount");
            Object paidAmountValue = razorpayLink.get("amount_paid");

            if (!(linkAmountValue instanceof Number)
                    || !(paidAmountValue instanceof Number)) {
                throw new InvalidTransactionException(
                        "Invalid amount returned for Payment Link");
            }

            int expectedAmount = payment.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .intValueExact();

            int linkAmount = ((Number) linkAmountValue).intValue();
            int paidAmount = ((Number) paidAmountValue).intValue();

            if (linkAmount != expectedAmount
                    || paidAmount != expectedAmount) {
                throw new InvalidTransactionException(
                        "Payment Link amount does not match the requested amount");
            }

            Payment razorpayPayment =
                    razorpayClient.payments.fetch(
                            request.getRazorpayPaymentId());

            String gatewayPaymentStatus = razorpayPayment.get("status");
            String gatewayPaymentCurrency = razorpayPayment.get("currency");
            String gatewayLinkId = razorpayPayment.get("payment_link_id");

            if (!"captured".equalsIgnoreCase(gatewayPaymentStatus)) {
                throw new InvalidTransactionException(
                        "Razorpay payment has not been captured");
            }

            if (!request.getRazorpayPaymentLinkId().equals(gatewayLinkId)) {
                throw new InvalidTransactionException(
                        "Payment does not belong to the specified Payment Link");
            }

            if (!"INR".equals(gatewayPaymentCurrency)) {
                throw new InvalidTransactionException(
                        "Payment currency is invalid");
            }

            Object paymentAmountValue = razorpayPayment.get("amount");

            if (!(paymentAmountValue instanceof Number)
                    || ((Number) paymentAmountValue).intValue()
                    != expectedAmount) {
                throw new InvalidTransactionException(
                        "Captured payment amount does not match");
            }

            WalletEntity wallet = walletRepository
                    .findWalletForUpdateByUserId(user.getUserId())
                    .orElseThrow(() ->
                            new WalletNotFoundException(
                                    "Wallet not found"));

            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            wallet.setBalance(
                    wallet.getBalance().add(payment.getAmount()));
            walletRepository.save(wallet);

            TransactionEntity transaction = new TransactionEntity();
            transaction.setSenderWallet(null);
            transaction.setReceiverWallet(wallet);
            transaction.setAmount(payment.getAmount());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setPayment(payment);

            transactionRepository.save(transaction);

            auditLogService.log(
                    user.getUserId(),
                    "PAYMENT_LINK_VERIFIED",
                    "Verified Payment Link "
                            + request.getRazorpayPaymentLinkId()
                            + " for amount " + payment.getAmount());

            return true;

        } catch (RazorpayException e) {
            log.error(
                    "Payment Link verification failed for linkId={}",
                    request.getRazorpayPaymentLinkId(),
                    e);

            throw new ExternalServiceException(
                    "Unable to verify payment with Razorpay",
                    e);
        }
    }

    @Transactional
    public void refundPayment(RefundRequestDTO request) {

        if (request == null
                || request.getRazorpayPaymentId() == null
                || request.getRazorpayPaymentId().isBlank()) {
            throw new InvalidTransactionException(
                    "Valid refund payment ID is required");
        }

        UserEntity user = authService.getAuthenticatedUser();

        PaymentEntity payment = paymentRepository
                .findByRazorpayPaymentIdForUpdate(
                        request.getRazorpayPaymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found"));

        if (!payment.getUser().getUserId().equals(user.getUserId())) {
            throw new InvalidTransactionException(
                    "Payment does not belong to authenticated user");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED
                || payment.getStatus() == PaymentStatus.REFUND_PENDING) {
            throw new InvalidTransactionException(
                    "Payment is already refunded or refund is pending");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidTransactionException(
                    "Only successful payments can be refunded");
        }

        WalletEntity wallet = walletRepository
                .findWalletForUpdateByUserId(user.getUserId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found"));

        if (wallet.getBalance().compareTo(payment.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient wallet balance for refund");
        }

        try {
            Refund refund = razorpayClient.payments.refund(
                    request.getRazorpayPaymentId());

            String refundId = refund.get("id");

            if (refundId == null || refundId.isBlank()) {
                throw new ExternalServiceException(
                        "Refund gateway returned no refund ID",
                        null);
            }

            payment.setRazorpayRefundId(refundId);
            payment.setRefundedAmount(payment.getAmount());
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            wallet.setBalance(
                    wallet.getBalance().subtract(payment.getAmount()));
            walletRepository.save(wallet);

            TransactionEntity transaction = new TransactionEntity();
            transaction.setSenderWallet(wallet);
            transaction.setReceiverWallet(null);
            transaction.setAmount(payment.getAmount());
            transaction.setType(TransactionType.REFUND);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setPayment(payment);

            transactionRepository.save(transaction);

            auditLogService.log(
                    user.getUserId(),
                    "REFUND",
                    "Refunded payment "
                            + request.getRazorpayPaymentId()
                            + " for amount " + payment.getAmount());

        } catch (RazorpayException e) {
            log.error(
                    "Razorpay refund failed for paymentId={}",
                    request.getRazorpayPaymentId(),
                    e);

            throw new ExternalServiceException(
                    "Refund request failed; verify gateway status before retrying",
                    e);
        }
    }
}
