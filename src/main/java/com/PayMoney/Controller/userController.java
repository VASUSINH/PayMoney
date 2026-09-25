package com.PayMoney.Controller;

import com.PayMoney.DTO.userRequestDTO;
import com.PayMoney.DTO.userResponseDTO;
import com.PayMoney.DTO.loginRequestDTO;
import com.PayMoney.DTO.loginResponseDTO;
import com.PayMoney.Service.userService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class  userController {

     @Autowired
     private userService userService;


    //method return type is userResponseDTO as controller class returns this DTO data.
    //This Line  (@RequestBody userRequestDTO add_userdto) creates a DTO Request Object.

    //Register user Endpoint.
    @PostMapping("api/register")
    public userResponseDTO createUser(@Valid @RequestBody userRequestDTO add_userdto){

        return userService.createUser(add_userdto);
    }
    //This is the Login endpoint.
    @PostMapping("/api/login")
    public loginResponseDTO login(@RequestBody loginRequestDTO loginRequest) {

        return userService.login(loginRequest);
    }

    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("api/allusers")
    public List<userResponseDTO> getAllUsers(){

        return userService.getAllUsers();
   }

    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/getUserById/{id}")
    public userResponseDTO getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }

    @SecurityRequirement(name = "bearer-key")
    @PutMapping("/api/updateUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public userResponseDTO updateUser(
            @PathVariable Long id,
            @RequestBody userRequestDTO userRequest) {

        return userService.updateUser(id, userRequest);
    }

    @SecurityRequirement(name = "bearer-key")
    @DeleteMapping("/api/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
