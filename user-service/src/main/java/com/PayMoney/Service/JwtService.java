package com.PayMoney.Service;

import com.PayMoney.Entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Service
public class JwtService {
private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
    public String generateToken(UserEntity userEntity){
        return Jwts.builder()
                .subject(userEntity.getEmail())
                .claim("role", userEntity.getRole())

                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserEntity user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail());
    }



}
