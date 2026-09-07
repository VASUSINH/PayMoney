package com.PayMoney.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class jwtService {
//This Class is Responsible to handle JWT Operations.
private final SecretKey secretKey;

    public jwtService() {
        this.secretKey = Keys.hmacShaKeyFor(
                "my-super-secret-key-for-payflow-jwt-2026".getBytes()
        );
    }
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
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

    public boolean isTokenValid(String token, user user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail());
    }
}
