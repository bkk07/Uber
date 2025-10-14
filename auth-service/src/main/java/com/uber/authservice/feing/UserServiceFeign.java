package com.uber.authservice.feing;
import com.uber.authservice.config.UserServiceErrorDecoder;
import com.uber.authservice.model.AuthRequest;
import com.uber.authservice.model.AuthResponse;
import com.uber.authservice.model.UserAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service",configuration = UserServiceErrorDecoder.class)
public interface UserServiceFeign {
    @GetMapping("/api/users/validate")
    UserAuthResponse userExists(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password
    );
}

