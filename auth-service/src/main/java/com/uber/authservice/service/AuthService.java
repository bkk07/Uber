package com.uber.authservice.service;
import com.uber.authservice.feing.AdminServiceFeign;
import com.uber.authservice.feing.DriverServiceFeign;
import com.uber.authservice.feing.UserServiceFeign;
import com.uber.authservice.model.AdminAuthResponse;
import com.uber.authservice.model.DriverAuthResponse;
import com.uber.authservice.model.UserAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserServiceFeign userServiceFeign;
    private final DriverServiceFeign driverServiceFeign;
    private final AdminServiceFeign adminServiceFeign;
    public UserAuthResponse authenticateUser(String loginId, String password) {
        return userServiceFeign.userExists(loginId,password);
    }
    public DriverAuthResponse authenticateDriver(String loginId, String password) {
        return driverServiceFeign.driverExists(loginId,password);
    }

    public AdminAuthResponse authenticateAdmin(String loginId, String password) {
        return adminServiceFeign.adminExists(loginId,password);
    }
}