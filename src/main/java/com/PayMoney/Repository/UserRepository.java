package com.PayMoney.Repository;

import com.PayMoney.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {

    boolean existsByEmailAndUserIdNot(String email, Long userId);

    Optional<UserEntity> findByEmail(String email);


}
