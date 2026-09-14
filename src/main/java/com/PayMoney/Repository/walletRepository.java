package com.PayMoney.Repository;

import com.PayMoney.Entity.walletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface walletRepository extends JpaRepository<walletEntity,Long> {


     Optional<walletEntity> findByUser_UserId(Long userId);
}
