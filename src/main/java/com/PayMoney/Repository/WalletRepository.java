package com.PayMoney.Repository;

import com.PayMoney.Entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity,Long> {

     Optional<WalletEntity> findByUser_UserId(Long userId);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("SELECT w FROM WalletEntity w WHERE w.walletId = :walletId")
     Optional<WalletEntity> findWalletForUpdate(@Param("walletId") Long walletId);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("""
       SELECT w FROM WalletEntity w WHERE w.user.userId = :userId
       """)
     Optional<WalletEntity> findWalletForUpdateByUserId(@Param("userId") Long userId);
}
