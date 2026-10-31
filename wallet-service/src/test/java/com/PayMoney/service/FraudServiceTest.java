package com.PayMoney.service;

import com.PayMoney.Entity.TransactionStatus;
import com.PayMoney.Entity.TransactionType;
import com.PayMoney.Repository.TransactionRepository;
import com.PayMoney.Service.FraudService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudServiceTest {

    @InjectMocks
    private FraudService fraudService;

    @Mock
    private TransactionRepository transactionRepository;

    private void mockRecentTransfers(long count) {
        when(transactionRepository
                .countBySenderWallet_WalletIdAndTypeAndStatusAndCreatedAtAfter(
                        eq(1L),
                        eq(TransactionType.TRANSFER),
                        eq(TransactionStatus.SUCCESS),
                        any(LocalDateTime.class)))
                .thenReturn(count);
    }

    @Test
    void isSuspicious_shouldReturnFalse_whenFewerThanFiveTransfersInLastMinute() {
        mockRecentTransfers(4);

        assertFalse(fraudService.isSuspicious(1L));
    }

    @Test
    void isSuspicious_shouldReturnTrue_whenFiveTransfersInLastMinute() {
        mockRecentTransfers(5);

        assertTrue(fraudService.isSuspicious(1L));
    }

    @Test
    void isSuspicious_shouldThrowException_whenWalletIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> fraudService.isSuspicious(null)
        );
    }
}
