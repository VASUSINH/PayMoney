
package com.PayMoney.service;

import com.PayMoney.DTO.RefundRequestDTO;
import com.PayMoney.Entity.PaymentEntity;
import com.PayMoney.Entity.PaymentStatus;
import com.PayMoney.Entity.TransactionEntity;
import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Entity.WalletEntity;
import com.PayMoney.Exception.ExternalServiceException;
import com.PayMoney.Exception.InsufficientBalanceException;
import com.PayMoney.Exception.InvalidTransactionException;
import com.PayMoney.Repository.PaymentRepository;
import com.PayMoney.Repository.TransactionRepository;
import com.PayMoney.Repository.WalletRepository;
import com.PayMoney.Service.AuditLogService;
import com.PayMoney.Service.AuthService;
import com.PayMoney.Service.PaymentService;
import com.razorpay.PaymentClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceRefundTest {

    private static final Long USER_ID = 1L;
    private static final String PAYMENT_ID = "pay_test123";
    private static final BigDecimal PAYMENT_AMOUNT =
            new BigDecimal("500.00");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PaymentService paymentService;

    private UserEntity user;
    private PaymentEntity payment;
    private WalletEntity wallet;
    private RefundRequestDTO request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                razorpayClient,
                "payments",
                paymentClient
        );

        user = new UserEntity();
        user.setUserId(USER_ID);

        payment = new PaymentEntity();
        payment.setUser(user);
        payment.setRazorpayPaymentId(PAYMENT_ID);
        payment.setAmount(PAYMENT_AMOUNT);
        payment.setStatus(PaymentStatus.SUCCESS);

        wallet = new WalletEntity();
        wallet.setBalance(new BigDecimal("1000.00"));

        request = new RefundRequestDTO();
        request.setRazorpayPaymentId(PAYMENT_ID);

        when(paymentRepository.findByRazorpayPaymentIdForUpdate(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        when(authService.getAuthenticatedUser())
                .thenReturn(user);


    }

    @Test
    void refundPaymentShouldRejectPaymentThatIsNotSuccessful() throws RazorpayException {
        payment.setStatus(PaymentStatus.CREATED);

        assertThrows(
                InvalidTransactionException.class,
                () -> paymentService.refundPayment(request)
        );

        verify(paymentClient, never()).refund(anyString());
        verify(paymentRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void refundPaymentShouldRejectAlreadyRefundedPayment() throws RazorpayException {
        payment.setStatus(PaymentStatus.REFUNDED);

        assertThrows(
                InvalidTransactionException.class,
                () -> paymentService.refundPayment(request)
        );

        verify(walletRepository, never())
                .findWalletForUpdateByUserId(any());

        verify(paymentClient, never()).refund(anyString());
        verify(paymentRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void refundPaymentShouldRejectInsufficientWalletBalance() throws RazorpayException {
        wallet.setBalance(new BigDecimal("100.00"));

        when(walletRepository.findWalletForUpdateByUserId(USER_ID))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> paymentService.refundPayment(request)
        );

        verify(paymentClient, never()).refund((String) any());
        verify(paymentRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void refundPaymentShouldThrowExternalServiceExceptionWhenRazorpayFails()
            throws Exception {

        when(walletRepository.findWalletForUpdateByUserId(USER_ID))
                .thenReturn(Optional.of(wallet));

        when(paymentClient.refund(PAYMENT_ID))
                .thenThrow(new RazorpayException("Gateway unavailable"));

        assertThrows(
                ExternalServiceException.class,
                () -> paymentService.refundPayment(request)
        );

        verify(paymentClient).refund(PAYMENT_ID);
        verify(paymentRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any());
    }
}
