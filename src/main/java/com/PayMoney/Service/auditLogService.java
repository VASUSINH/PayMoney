package com.PayMoney.Service;

import com.PayMoney.Entity.auditLogEntity;
import com.PayMoney.Repository.auditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class auditLogService {

    @Autowired
    private auditLogRepository auditLogRepository;

    public void log(Long userId, String action, String description) {

        auditLogEntity auditLog = new auditLogEntity();

        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }
}