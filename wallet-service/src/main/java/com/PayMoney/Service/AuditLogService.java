package com.PayMoney.Service;

import com.PayMoney.DTO.AuditLogEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.audit-log}")
    private String auditLogTopic;

    // audit-service consumes this event and stores the audit log.
    public void log(Long userId, String action, String description) {

        AuditLogEventDTO auditLog = new AuditLogEventDTO(
                userId, action, description, LocalDateTime.now());

        String key = userId != null ? String.valueOf(userId) : null;

        // If we are inside a database transaction, wait until it has
        // committed. That way a rolled back transfer never gets an audit
        // log, and Kafka is not called while wallet rows are locked.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            send(key, auditLog);
                        }
                    });
            return;
        }

        send(key, auditLog);
    }

    private void send(String key, AuditLogEventDTO auditLog) {

        // An audit log problem must never make a finished payment fail.
        try {
            kafkaTemplate.send(auditLogTopic, key, auditLog)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error("Failed to publish audit log event {}",
                                    auditLog.getAction(), exception);
                        }
                    });
        } catch (RuntimeException exception) {
            log.error("Failed to publish audit log event {}",
                    auditLog.getAction(), exception);
        }
    }
}
