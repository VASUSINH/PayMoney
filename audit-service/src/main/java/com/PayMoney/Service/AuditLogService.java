package com.PayMoney.Service;

import com.PayMoney.Entity.AuditLogEntity;
import com.PayMoney.Repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(Long userId, String action, String description,
                    LocalDateTime timestamp) {

        AuditLogEntity auditLog = new AuditLogEntity();

        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setTimestamp(timestamp);

        auditLogRepository.save(auditLog);
    }
}
