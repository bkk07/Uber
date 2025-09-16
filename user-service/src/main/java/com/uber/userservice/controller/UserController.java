package com.uber.userservice.controller;
import com.uber.userservice.dto.UserAuthResponse;
import com.uber.userservice.dto.UserRegisterRequest;
import com.uber.userservice.dto.UserResponse;
import com.uber.userservice.service.UserService;
import com.uber.userservice.dto.UserUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse newUser = userService.registerUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content for successful deletion
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }


    @GetMapping("/validate")
    ResponseEntity<UserAuthResponse> userExists(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password
    ){
        System.out.println("I am in user-service "+loginId+" "+password);
        UserAuthResponse userAuthResponse = userService.userExists(loginId,password);
        System.out.println("This is the response going from user service to the authservice "+userAuthResponse.toString());
        return ResponseEntity.ok(userAuthResponse);
    }
}