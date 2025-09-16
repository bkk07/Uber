package com.uber.adminservice.controller;

import com.uber.adminservice.dto.AdminAuthResponse;
import com.uber.adminservice.dto.AdminRequest;
import com.uber.adminservice.dto.AdminResponse;
import com.uber.adminservice.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    @PostMapping("/register")
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody AdminRequest adminRequest) {
        return ResponseEntity.ok(adminService.createAdmin(adminRequest));
    }
    @GetMapping("/validate")
    ResponseEntity<AdminAuthResponse> adminExists(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password
    ){
        System.out.println("I am in admin-service "+loginId+" "+password);
        AdminAuthResponse adminAuthResponse =adminService.adminExists(loginId,password);
        System.out.println("This is the response going from admin-service to the auth-service"+adminAuthResponse.toString());
        return ResponseEntity.ok(adminAuthResponse);
    }
}
