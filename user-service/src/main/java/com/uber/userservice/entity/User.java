package com.uber.userservice.entity;
import com.uber.userservice.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Username cannot be empty")
    @Column(unique = true, nullable = false)
    private String username;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;
    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Long addressId; // Refers to AddressService

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "User role cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole=UserRole.USER;

    // This field will be managed by Spring Security (e.g., encoded)
    @NotBlank(message = "Password cannot be empty")
    @Column(nullable = false)
    private String password;
}