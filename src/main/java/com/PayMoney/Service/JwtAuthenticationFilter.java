package com.PayMoney.Service;

import com.PayMoney.Entity.userEntity;
import com.PayMoney.Repository.userRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Checks the HTTP request contains JWT or Not.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private jwtService jwtService;

    @Autowired
    private userRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {


        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        String email = jwtService.extractEmail(token);

        userEntity userEntity = userRepository
                .findByEmail(email)
                .orElse(null);

        if (userEntity != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userEntity,
                            null,
                            userEntity.getAuthorities()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}

