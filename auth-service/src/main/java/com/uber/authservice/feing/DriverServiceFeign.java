package com.uber.authservice.feing;

import com.uber.authservice.model.DriverAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="driver-service")
public interface DriverServiceFeign {
    @GetMapping("/api/drivers/validate")
    DriverAuthResponse driverExists(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password
    );
}
