package com.uber.authservice.controller;
import com.uber.authservice.model.*;
import com.uber.authservice.service.AuthService;
import com.uber.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    // Auth Service Controller
    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            UserAuthResponse response = authService.authenticateUser(authRequest.getLoginId(), authRequest.getPassword());
            System.out.println("User Auth Response :"+response.toString());
            String token = jwtUtil.generateToken(response.getUserId(), response.getRole());
            return ResponseEntity.ok(new AuthResponse(token, response.getUserId(), response.getRole()));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Username or Email not Found.."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Invalid Password.."));
        }
    }
    @GetMapping("/test-token")
    public ResponseEntity<String> testTokenGeneration(@RequestParam Long userId, @RequestParam String role) {
        String token = jwtUtil.generateToken(userId, role);
        return ResponseEntity.ok("Generated Token: " + token);
    }
    @PostMapping("/drivers/login")
    public ResponseEntity<?> loginDriver(@RequestBody AuthRequest authRequest) {
        try {
            DriverAuthResponse response = authService.authenticateDriver(authRequest.getLoginId(),authRequest.getPassword());
            System.out.println("Driver Auth Response :"+response.toString());
            String token = jwtUtil.generateToken(response.getUserId(), response.getRole());
            return ResponseEntity.ok(new AuthResponse(token, response.getUserId(), response.getRole()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 🔥 log the exact error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication.");
        }
    }
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody AuthRequest authRequest) {
        try {
            AdminAuthResponse response = authService.authenticateAdmin(authRequest.getLoginId(),authRequest.getPassword());
            System.out.println("Admin Auth Response :"+response.toString());
            String token = jwtUtil.generateToken(response.getUserId(), response.getRole());
            return ResponseEntity.ok(new AuthResponse(token, response.getUserId(), response.getRole()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 🔥 log the exact error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication.");
        }

    }

}