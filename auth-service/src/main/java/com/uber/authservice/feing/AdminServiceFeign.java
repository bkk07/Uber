package com.uber.authservice.feing;

import com.uber.authservice.model.AdminAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admin-service")
public interface AdminServiceFeign {
    @GetMapping("/api/admin/validate")
    AdminAuthResponse adminExists(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password
    );
}
