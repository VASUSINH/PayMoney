package com.PayMoney.service;

import com.PayMoney.Entity.AuditLogEntity;
import com.PayMoney.Repository.AuditLogRepository;
import com.PayMoney.Service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void log_shouldSaveAuditLogWithGivenValues() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 15, 10, 30);

        auditLogService.log(7L, "TRANSFER", "Transferred 100", time);

        ArgumentCaptor<AuditLogEntity> captor =
                ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntity saved = captor.getValue();
        assertEquals(7L, saved.getUserId());
        assertEquals("TRANSFER", saved.getAction());
        assertEquals("Transferred 100", saved.getDescription());
        assertEquals(time, saved.getTimestamp());
    }
}
