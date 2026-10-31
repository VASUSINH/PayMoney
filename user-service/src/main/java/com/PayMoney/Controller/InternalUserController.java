package com.PayMoney.Controller;

import com.PayMoney.DTO.UserAuthResponseDTO;
import com.PayMoney.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Called only by the other PayMoney services (not routed by the API Gateway).
// Callers must send the shared X-Internal-Key header.
@RestController
public class InternalUserController {

    @Autowired
    private UserService userService;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @GetMapping("/api/v1/internal/users/by-email")
    public UserAuthResponseDTO getUserByEmail(
            @RequestParam("email") String email,
            @RequestHeader(value = "X-Internal-Key", required = false)
            String internalKey) {

        if (!internalApiKey.equals(internalKey)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Invalid internal key");
        }

        return userService.getUserByEmail(email);
    }
}
