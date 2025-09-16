package com.uber.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RideUserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
}
