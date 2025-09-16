package com.uber.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRequest {
    @NotBlank(message = "Username or email cannot be empty")
    private String usernameOrEmail;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}