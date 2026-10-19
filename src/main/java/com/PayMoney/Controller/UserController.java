package com.PayMoney.Controller;

import com.PayMoney.DTO.*;
import com.PayMoney.Service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("api/allusers")
    public List<UserResponseDTO> getAllUsers(){

        return userService.getAllUsers();
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
