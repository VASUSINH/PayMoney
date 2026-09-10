package com.PayMoney.Service;

import com.PayMoney.Entity.userEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class jwtService {
//This Class is Responsible to handle JWT Operations(create,extract,validate).
private final SecretKey secretKey;

    public jwtService() {
        this.secretKey = Keys.hmacShaKeyFor(
                "my-super-secret-key-for-payflow-jwt-2026".getBytes()
        );
    }
    // This Method is Responsible to Generate Tokens using email.
    public String generateToken(userEntity userEntity){
        return Jwts.builder()
                .subject(userEntity.getEmail())
                .claim("role", userEntity.getRole())

                .signWith(secretKey)
                .compact();
    }

    // Extract the email from the Token recieved on Subsequent request after login.
    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // It validates the Tokens.
    public boolean isTokenValid(String token, userEntity user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail());
    }



}
