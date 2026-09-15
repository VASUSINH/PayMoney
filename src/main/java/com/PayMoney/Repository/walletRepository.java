package com.PayMoney.Repository;

import com.PayMoney.Entity.walletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface walletRepository extends JpaRepository<walletEntity,Long> {

     Optional<walletEntity> findByUser_UserId(Long userId);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("SELECT w FROM walletEntity w WHERE w.walletId = :walletId")
     Optional<walletEntity> findWalletForUpdate(@Param("walletId") Long walletId);
}
