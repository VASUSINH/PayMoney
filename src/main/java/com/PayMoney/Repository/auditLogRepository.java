package com.PayMoney.Repository;

import com.PayMoney.Entity.auditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface auditLogRepository
        extends JpaRepository<auditLogEntity, Long> {
}