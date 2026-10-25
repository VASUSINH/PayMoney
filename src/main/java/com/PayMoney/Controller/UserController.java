package com.PayMoney.Controller;

import com.PayMoney.DTO.*;
import com.PayMoney.Service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class UserController {

     @Autowired
     private UserService userService;



    @PostMapping("/api/v1/register-user")
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO add_userdto){

        return userService.register(add_userdto);
    }

    @PostMapping("/api/v1/auth/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO loginRequest) {

        return userService.login(loginRequest);
    }

    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/all-users")
    public Page<UserResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be >= 0 and size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "userId")
        );

        return userService.getAllUsers(pageable);
    }

    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/users/{userId}")
    public UserResponseDTO getUserById(@PathVariable Long userId) {

        return userService.getUserById(userId);
    }


    @SecurityRequirement(name = "bearer-key")
    @PutMapping("/api/v1/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateDTO userRequest) {

        return userService.updateUser(userId, userRequest);
    }

    @PatchMapping("/api/v1/users/me/deactivate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> deactivateMyAccount() {

        userService.deactivateMyAccount();

        return ResponseEntity.ok(
                Map.of("message", "Account successfully deactivated")
        );
    }

    @PostMapping("/api/v1/auth/reactivate")
    public ResponseEntity<Map<String, String>> reactivateMyAccount(
            @Valid @RequestBody ReactivateAccountRequestDTO request) {

        userService.reactivateMyAccount(request);

        return ResponseEntity.ok(
                Map.of("message", "Account successfully reactivated")
        );
    }
}
