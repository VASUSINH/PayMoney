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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class UserController {

     @Autowired
     private UserService userService;



    @PostMapping("api/register")
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO add_userdto){

        return userService.register(add_userdto);
    }

    @PostMapping("/api/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO loginRequest) {

        return userService.login(loginRequest);
    }

    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/allusers")
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
    @GetMapping("/api/getUserById/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }


    @SecurityRequirement(name = "bearer-key")
    @PutMapping("/api/updateUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUser(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateDTO userRequest) {

        return userService.updateUser(id, userRequest);
    }


    @SecurityRequirement(name = "bearer-key")
    @DeleteMapping("/api/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
