package com.aditya.xenop.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // for normal boiler plate code(getter setter...)
public class RegisterRequest {

    @NotBlank(message = "Username is Required")
    @Size(min = 3 , max = 50)
    private String username;

    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Email format")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 6 , message = "Password must be at least 6 characters")
    private String password;
}
