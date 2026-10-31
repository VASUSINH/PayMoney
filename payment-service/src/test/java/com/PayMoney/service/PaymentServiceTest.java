package com.PayMoney.service;

import com.PayMoney.Client.WalletClient;
import com.PayMoney.DTO.PaymentLinkVerificationRequestDTO;
import com.PayMoney.DTO.PaymentRequestDTO;
import com.PayMoney.Exception.InvalidTransactionException;
import com.PayMoney.Repository.PaymentRepository;
import com.PayMoney.Service.AuditLogService;
import com.PayMoney.Service.AuthService;
import com.PayMoney.Service.PaymentService;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuthService authService;

    @Mock
    private WalletClient walletClient;

    @Mock
    private AuditLogService auditLogService;

    @Test
    void createPaymentLink_shouldThrowException_whenAmountIsOutOfRange() {
        PaymentRequestDTO tooSmall = new PaymentRequestDTO();
        tooSmall.setAmount(new BigDecimal("0.50"));

        PaymentRequestDTO tooLarge = new PaymentRequestDTO();
        tooLarge.setAmount(new BigDecimal("60000"));

        assertThrows(
                InvalidTransactionException.class,
                () -> paymentService.createPaymentLink(tooSmall)
        );
        assertThrows(
                InvalidTransactionException.class,
                () -> paymentService.createPaymentLink(tooLarge)
        );

        verifyNoInteractions(paymentRepository, walletClient);
    }

    @Test
    void verifyPaymentLinkCallback_shouldThrowException_whenLinkIsNotPaid() {
        PaymentLinkVerificationRequestDTO request =
                new PaymentLinkVerificationRequestDTO();
        request.setRazorpayPaymentLinkId("plink_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpayPaymentLinkReferenceId("ref-123");
        request.setRazorpayPaymentLinkStatus("created");
        request.setRazorpaySignature("signature");

        assertThrows(
                InvalidTransactionException.class,
                () -> paymentService.verifyPaymentLinkCallback(request)
        );

        verifyNoInteractions(paymentRepository, walletClient);
    }
}
