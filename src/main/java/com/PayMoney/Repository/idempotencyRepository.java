package com.PayMoney.Repository;

import com.PayMoney.Entity.idempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface idempotencyRepository
        extends JpaRepository<idempotencyEntity, Long> {

    Optional<idempotencyEntity> findByIdempotencyKey(
            String idempotencyKey);
}
