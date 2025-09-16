package com.uber.adminservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdminResponse {
    private Long id;
    private String username;
    private String phone;
    private  String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
