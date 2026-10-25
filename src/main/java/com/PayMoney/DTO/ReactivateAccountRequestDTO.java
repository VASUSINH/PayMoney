package com.PayMoney.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReactivateAccountRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}