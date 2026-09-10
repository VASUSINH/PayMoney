package com.PayMoney.Controller;

import com.PayMoney.DTO.addUserRequestDTO;
import com.PayMoney.DTO.addUserResponseDTO;
import com.PayMoney.DTO.loginRequestDTO;
import com.PayMoney.DTO.loginResponseDTO;
import com.PayMoney.Service.userService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class  userController {

     @Autowired
     private userService userService;


    //method return type is addUserResponseDTO as controller class returns this DTO data.
    //This Line  (@RequestBody addUserRequestDTO add_userdto) creates a DTO Request Object.

    //Register user Endpoint.
    @PostMapping("api/addusers")
    public addUserResponseDTO createUser(@Valid @RequestBody addUserRequestDTO add_userdto){

        return userService.createUser(add_userdto);
    }
    //This is the Login endpoint.
    @PostMapping("/api/login")
    public loginResponseDTO login(@RequestBody loginRequestDTO loginRequest) {

        return userService.login(loginRequest);
    }


}
