
package com.PayMoney.Service;

import com.PayMoney.Client.WalletClient;
import com.PayMoney.DTO.PaymentDepositRequestDTO;
import com.PayMoney.DTO.PaymentLinkVerificationRequestDTO;
import com.PayMoney.DTO.PaymentRequestDTO;
import com.PayMoney.DTO.PaymentResponseDTO;
import com.PayMoney.DTO.UserAuthResponseDTO;
import com.PayMoney.Entity.*;
import com.PayMoney.Exception.*;
import com.PayMoney.Repository.PaymentRepository;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private WalletClient walletClient;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    public PaymentResponseDTO createPaymentLink(
            PaymentRequestDTO request) {

        if (request == null || request.getAmount() == null) {
            throw new InvalidTransactionException(
                    "Payment amount is required");
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(new BigDecimal("1.00")) < 0
                || amount.compareTo(new BigDecimal("50000.00")) > 0
                || amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidTransactionException(
                    "Amount must be between 1 and 50000 "
                            + "with at most two decimal places");
        }

        UserAuthResponseDTO user = authService.getAuthenticatedUser();

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
        linkRequest.put(
                "callback_url",
                publicBaseUrl + "/api/v1/payment-links/callback");
        linkRequest.put("callback_method", "get");

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
            payment.setUserId(user.getUserId());
            payment.setAmount(amount);
            payment.setRazorpayPaymentLinkId(linkId);
            payment.setRazorpayPaymentLinkReferenceId(referenceId);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setWalletCredited(false);

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

        UserAuthResponseDTO user = authService.getAuthenticatedUser();

        return verifyPaymentLinkInternal(request, user);
    }

    @Transactional
    public boolean verifyPaymentLinkCallback(
            PaymentLinkVerificationRequestDTO request) {

        return verifyPaymentLinkInternal(request, null);
    }

    private boolean verifyPaymentLinkInternal(
            PaymentLinkVerificationRequestDTO request,
            UserAuthResponseDTO authenticatedUser) {

        if (request == null
                || request.getRazorpayPaymentLinkId() == null
                || request.getRazorpayPaymentLinkId().isBlank()
                || request.getRazorpayPaymentId() == null
                || request.getRazorpayPaymentId().isBlank()
                || request.getRazorpayPaymentLinkReferenceId() == null
                || request.getRazorpayPaymentLinkReferenceId().isBlank()
                || request.getRazorpayPaymentLinkStatus() == null
                || request.getRazorpayPaymentLinkStatus().isBlank()
                || request.getRazorpaySignature() == null
                || request.getRazorpaySignature().isBlank()) {
            throw new InvalidTransactionException(
                    "Required payment verification details are missing");
        }

        if (!"paid".equalsIgnoreCase(
                request.getRazorpayPaymentLinkStatus())) {
            throw new InvalidTransactionException(
                    "Payment Link has not been paid");
        }

        try {
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

            Utils.verifyPaymentLink(signatureData, keySecret);

            PaymentEntity payment = paymentRepository
                    .findByRazorpayPaymentLinkIdForUpdate(
                            request.getRazorpayPaymentLinkId())
                    .orElseThrow(() ->
                            new InvalidTransactionException(
                                    "Payment Link not found"));

            if (!payment.getRazorpayPaymentLinkReferenceId().equals(
                    request.getRazorpayPaymentLinkReferenceId())) {
                throw new InvalidTransactionException(
                        "Payment reference ID does not match");
            }

            if (authenticatedUser != null
                    && !payment.getUserId()
                    .equals(authenticatedUser.getUserId())) {
                throw new InvalidTransactionException(
                        "Payment Link does not belong to authenticated user");
            }

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                if (payment.isWalletCredited()
                        && request.getRazorpayPaymentId().equals(
                        payment.getRazorpayPaymentId())) {
                    return true;
                }

                throw new InvalidTransactionException(
                        "Payment has already been processed "
                                + "or its state is inconsistent");
            }

            if (payment.isWalletCredited()) {
                throw new InvalidTransactionException(
                        "Wallet credit state is inconsistent");
            }

            if (payment.getStatus() != PaymentStatus.CREATED) {
                throw new InvalidTransactionException(
                        "Payment is not in a verifiable state");
            }

            PaymentLink razorpayLink =
                    razorpayClient.paymentLink.fetch(
                            request.getRazorpayPaymentLinkId());

            if (!"paid".equalsIgnoreCase(
                    razorpayLink.get("status").toString())) {
                throw new InvalidTransactionException(
                        "Razorpay Payment Link is not paid");
            }

            if (!payment.getRazorpayPaymentLinkReferenceId().equals(
                    razorpayLink.get("reference_id").toString())) {
                throw new InvalidTransactionException(
                        "Razorpay Payment Link reference does not match");
            }

            if (!"INR".equalsIgnoreCase(
                    razorpayLink.get("currency").toString())) {
                throw new InvalidTransactionException(
                        "Unsupported payment currency");
            }

            long expectedAmountPaise = payment.getAmount()
                    .multiply(new BigDecimal("100"))
                    .longValueExact();

            long linkAmount =
                    ((Number) razorpayLink.get("amount")).longValue();

            long amountPaid =
                    ((Number) razorpayLink.get("amount_paid")).longValue();

            if (linkAmount != expectedAmountPaise
                    || amountPaid != expectedAmountPaise) {
                throw new InvalidTransactionException(
                        "Payment amount does not match the requested amount");
            }

            Payment razorpayPayment =
                    razorpayClient.payments.fetch(
                            request.getRazorpayPaymentId());

            if (!"captured".equalsIgnoreCase(
                    razorpayPayment.get("status").toString())) {
                throw new InvalidTransactionException(
                        "Razorpay payment has not been captured");
            }

            Object paymentsObject = razorpayLink.get("payments");

            if (!(paymentsObject instanceof JSONArray linkPayments)) {
                throw new InvalidTransactionException(
                        "Unable to verify payments for this Payment Link");
            }

            boolean matchingCapturedPayment = false;

            for (int i = 0; i < linkPayments.length(); i++) {
                JSONObject linkedPayment =
                        linkPayments.getJSONObject(i);

                String linkedPaymentId =
                        linkedPayment.optString("payment_id");

                String linkedPaymentStatus =
                        linkedPayment.optString("status");

                if (request.getRazorpayPaymentId().equals(linkedPaymentId)
                        && "captured".equalsIgnoreCase(
                        linkedPaymentStatus)) {
                    matchingCapturedPayment = true;
                    break;
                }
            }

            if (!matchingCapturedPayment) {
                throw new InvalidTransactionException(
                        "Captured payment does not match this Payment Link");
            }

            if (!"INR".equalsIgnoreCase(
                    razorpayPayment.get("currency").toString())) {
                throw new InvalidTransactionException(
                        "Unsupported payment currency");
            }

            long actualPaymentAmount =
                    ((Number) razorpayPayment.get("amount")).longValue();

            if (actualPaymentAmount != expectedAmountPaise) {
                throw new InvalidTransactionException(
                        "Captured payment amount does not match");
            }

            Long userId = payment.getUserId();

            // wallet-service credits the wallet and records the DEPOSIT
            // transaction; it is idempotent per payment ID, so a retry
            // after a failure never credits the wallet twice.
            walletClient.deposit(new PaymentDepositRequestDTO(
                    userId,
                    payment.getPaymentId(),
                    payment.getAmount()));

            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());
            payment.setWalletCredited(true);
            payment.setStatus(PaymentStatus.SUCCESS);

            paymentRepository.save(payment);

            auditLogService.log(
                    userId,
                    "PAYMENT_LINK_VERIFIED",
                    "Payment verified and wallet credited");

            return true;

        } catch (RazorpayException e) {
            throw new ExternalServiceException(
                    "Unable to verify payment with Razorpay",
                    e);
        }
    }
}
