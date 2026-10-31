package com.PayMoney.Consumer;

import com.PayMoney.DTO.AuditLogEventDTO;
import com.PayMoney.Service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLogConsumer {

    @Autowired
    private AuditLogService auditLogService;

    @KafkaListener(topics = "${app.kafka.topics.audit-log}")
    public void onAuditLog(AuditLogEventDTO event) {

        auditLogService.log(
                event.getUserId(),
                event.getAction(),
                event.getDescription(),
                event.getTimestamp());
    }
}
