package com.PayMoney.Service;

import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Exception.UserNotFoundException;
import com.PayMoney.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public UserEntity getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();

        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }
}