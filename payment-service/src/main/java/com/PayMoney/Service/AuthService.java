package com.PayMoney.Service;

import com.PayMoney.DTO.UserAuthResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    // The principal is the user loaded from user-service by
    // JwtAuthenticationFilter for the current request.
    public UserAuthResponseDTO getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return (UserAuthResponseDTO) authentication.getPrincipal();
    }
}
