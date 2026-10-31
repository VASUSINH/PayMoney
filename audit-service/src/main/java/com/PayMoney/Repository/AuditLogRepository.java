package com.PayMoney.Repository;

import com.PayMoney.Entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLogEntity, Long> {
}