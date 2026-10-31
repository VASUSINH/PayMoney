package com.PayMoney.service;

import com.PayMoney.Consumer.AuditLogConsumer;
import com.PayMoney.DTO.AuditLogEventDTO;
import com.PayMoney.Service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogConsumerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogConsumer auditLogConsumer;

    @Test
    void onAuditLog_shouldPassEventToAuditLogService() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 15, 10, 30);
        AuditLogEventDTO event = new AuditLogEventDTO(
                7L, "WITHDRAW", "Withdrew 50", time);

        auditLogConsumer.onAuditLog(event);

        verify(auditLogService).log(7L, "WITHDRAW", "Withdrew 50", time);
    }
}
