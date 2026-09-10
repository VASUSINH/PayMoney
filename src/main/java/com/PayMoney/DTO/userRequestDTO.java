package com.PayMoney.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class userRequestDTO {
    @NotBlank(message="Name is Required")
    private String name;

    @NotBlank(message="Email is Required")
    @Email(message="Enter Correct Email Format")
    private String email;

    @NotBlank(message="Password is Required")
    @Size(min=8, message="Password must be at least contain 8 characters")
    private String password;
}
