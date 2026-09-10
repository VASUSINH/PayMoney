package com.PayMoney.Controller;

import com.PayMoney.DTO.userRequestDTO;
import com.PayMoney.DTO.userResponseDTO;
import com.PayMoney.DTO.loginRequestDTO;
import com.PayMoney.DTO.loginResponseDTO;
import com.PayMoney.Service.userService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class  userController {

     @Autowired
     private userService userService;


    //method return type is userResponseDTO as controller class returns this DTO data.
    //This Line  (@RequestBody userRequestDTO add_userdto) creates a DTO Request Object.

    //Register user Endpoint.
    @PostMapping("api/addusers")
    public userResponseDTO createUser(@Valid @RequestBody userRequestDTO add_userdto){

        return userService.createUser(add_userdto);
    }
    //This is the Login endpoint.
    @PostMapping("/api/login")
    public loginResponseDTO login(@RequestBody loginRequestDTO loginRequest) {

        return userService.login(loginRequest);
    }
   @GetMapping("api/allusers")
    public List<userResponseDTO> getAllUsers(){
        return userService.getAllUsers();
   }
    @GetMapping("/api/users/{id}")
    public userResponseDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

}
