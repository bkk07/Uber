package com.uber.rideservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String authorizationHeader = getAuthorizationHeader();
            if (authorizationHeader != null) {
                // Pass the full "Bearer <token>" string
                requestTemplate.header("Authorization", authorizationHeader);
            }
        };
    }

    private String getAuthorizationHeader() {
        try {
            // Get the current HTTP request attributes
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                // This happens in background threads or non-HTTP contexts, which is often OK for service calls.
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("Authorization"); // Returns "Bearer <token>"
        } catch (IllegalStateException e) {
            // Context not available (e.g., async or non-web thread), logging may be useful here.
            return null;
        }
    }
}