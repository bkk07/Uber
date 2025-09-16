package com.uber.userservice.service;
import com.uber.userservice.dto.*;

import java.util.List;
public interface UserService {
    UserResponse registerUser(UserRegisterRequest request);
    // Placeholder for login - typically returns a token/user details
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    UserAuthResponse userExists(String loginId, String password);
    RideUserResponse getUserSummary(Long id);
}