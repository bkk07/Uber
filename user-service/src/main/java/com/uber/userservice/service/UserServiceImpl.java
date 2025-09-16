package com.uber.userservice.service;
import com.uber.userservice.dto.UserAuthResponse;
import com.uber.userservice.dto.UserRegisterRequest;
import com.uber.userservice.dto.UserResponse;
import com.uber.userservice.dto.UserUpdateRequest;
import com.uber.userservice.entity.User;
import com.uber.userservice.enums.UserRole;
import com.uber.userservice.exception.DuplicateUserException;
import com.uber.userservice.exception.UserNotFoundException;
import com.uber.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @Override
    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username '" + request.getUsername() + "' already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email '" + request.getEmail() + "' already exists.");
        }
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .addressId(request.getAddressId())
                .userRole(UserRole.USER) // Default role for new registrations
                 .password(passwordEncoder.encode(request.getPassword()))
                .build();
        newUser = userRepository.save(newUser);
        return mapToUserResponse(newUser);
    }
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }


    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail()) && !existingUser.getEmail().equals(request.getEmail())) {
                throw new DuplicateUserException("Email '" + request.getEmail() + "' already exists for another user.");
            }
            existingUser.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            existingUser.setPhone(request.getPhone());
        }

        if (request.getAddressId() != null) {
            existingUser.setAddressId(request.getAddressId());
        }

        if (request.getUserRole() != null) {
            existingUser.setUserRole(request.getUserRole());
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserAuthResponse userExists(String loginId, String password) {
        User credential = null;
        if (loginId.contains("@")) {
            credential = userRepository.findByEmail(loginId).orElse(null);
        } else {
            credential = userRepository.findByUsername(loginId).orElse(null);
        }
        if (credential == null) {
            throw new UsernameNotFoundException("User not found with loginId: " + loginId);
        }
        // Validate password
        if (!passwordEncoder.matches(password, credential.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        // Return Auth response
        UserAuthResponse userAuthResponse = new UserAuthResponse();
        userAuthResponse.setUserId(credential.getId());
        userAuthResponse.setRole(String.valueOf(credential.getUserRole()));
        return userAuthResponse;
    }
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .addressId(user.getAddressId())
                .createdAt(user.getCreatedAt())
                .userRole(user.getUserRole())
                .build();
    }
}