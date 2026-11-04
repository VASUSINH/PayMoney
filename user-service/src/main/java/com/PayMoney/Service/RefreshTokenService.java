package com.PayMoney.Service;

import com.PayMoney.Entity.RefreshTokenEntity;
import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Duration expiration;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${jwt.refresh-token-expiration}")
            Duration expiration) {

        this.repository = repository;
        this.expiration = expiration;
    }

    @Transactional
    public String create(UserEntity user) {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        RefreshTokenEntity entity =
                new RefreshTokenEntity();

        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(
                Instant.now().plus(expiration));

        repository.save(entity);

        return rawToken;
    }

    @Transactional
    public UserEntity rotate(String rawToken) {

        RefreshTokenEntity entity =
                repository.findByTokenHash(hash(rawToken))
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token"));

        if (entity.isRevoked()
                || entity.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired or revoked");
        }

        entity.setRevoked(true);

        return entity.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {

        repository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    repository.save(token);
                });
    }

    private String hash(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashed = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .encodeToString(hashed);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}