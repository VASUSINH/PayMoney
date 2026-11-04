package com.PayMoney.Service;

import com.PayMoney.DTO.LoginResponseDTO;
import com.PayMoney.Entity.UserEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            ObjectMapper objectMapper) {

        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User googleUser = oauthToken.getPrincipal();

        Map<String, Object> attributes = googleUser.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        Boolean emailVerified =
                (Boolean) attributes.get("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Google email is not verified"
            );
            return;
        }

        UserEntity user =
                userService.findOrCreateGoogleUser(email, name);

        String accessToken =
                jwtService.generateToken(user);

        String refreshToken =
                refreshTokenService.create(user);

        LoginResponseDTO loginResponse =
                new LoginResponseDTO(
                        accessToken,
                        refreshToken,
                        jwtService.getAccessTokenExpirationSeconds()
                );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                loginResponse
        );
    }
}