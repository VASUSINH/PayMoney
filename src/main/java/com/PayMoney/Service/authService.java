package com.PayMoney.Service;

import com.PayMoney.Entity.userEntity;
import com.PayMoney.Exception.userNotFoundException;
import com.PayMoney.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class authService {

    @Autowired
    private userRepository userRepository;

    public userEntity getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new userNotFoundException("User not found"));
    }
}