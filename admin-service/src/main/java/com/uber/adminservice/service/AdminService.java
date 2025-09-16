package com.uber.adminservice.service;
import com.uber.adminservice.AdminRepository;
import com.uber.adminservice.dto.AdminAuthResponse;
import com.uber.adminservice.dto.AdminRequest;
import com.uber.adminservice.dto.AdminResponse;
import com.uber.adminservice.exception.EmailAlreadyExistsException;
import com.uber.adminservice.exception.UserNameAlreadyExistsException;
import com.uber.adminservice.exception.UsernameNotFoundException;
import com.uber.adminservice.model.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    public AdminResponse createAdmin(AdminRequest adminRequest) {
        if (adminRepository.existsByUsername(adminRequest.getUsername())) {
            throw new UserNameAlreadyExistsException(
                    "User with username " + adminRequest.getUsername() + " already exists"
            );
        }

        if (adminRepository.existsByEmail(adminRequest.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "User with email " + adminRequest.getEmail() + " already exists"
            );
        }

        Admin admin = Admin.builder()
                .username(adminRequest.getUsername())
                .email(adminRequest.getEmail())
                .password(passwordEncoder.encode(adminRequest.getPassword()))
                .phone(adminRequest.getPhone())
                .build();

        Admin savedAdmin = adminRepository.save(admin);

        return AdminResponse.builder()
                .id(savedAdmin.getId())
                .username(savedAdmin.getUsername())
                .phone(savedAdmin.getPhone())
                .email(savedAdmin.getEmail())
                .createdAt(savedAdmin.getCreatedAt())
                .updatedAt(savedAdmin.getUpdatedAt())
                .build();
    }

    public AdminAuthResponse adminExists(String loginId, String password) {
        Admin credential = loginId.contains("@")
                ? adminRepository.findByEmail(loginId).orElseThrow(
                () -> new UsernameNotFoundException("Admin not found with email: " + loginId))
                : adminRepository.findByUsername(loginId).orElseThrow(
                () -> new UsernameNotFoundException("Admin not found with username: " + loginId));

        if (!passwordEncoder.matches(password, credential.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return AdminAuthResponse.builder()
                .userId(credential.getId())
                .role("ADMIN")
                .build();
    }
}
