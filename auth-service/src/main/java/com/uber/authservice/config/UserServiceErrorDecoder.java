package com.uber.authservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;

public class UserServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // Read the response body to inspect the error message/details
        String responseBody = "";
        try (var body = response.body().asReader()) {
            responseBody = new java.util.Scanner(body).useDelimiter("\\A").next();
        } catch (Exception e) {
            // Log this exception if reading the body fails
        }

        // 1. Check for the specific error messages you know the User Service sends
        if (response.status() == 500) {
            if (responseBody.contains("User not found")) {
                // Re-throw as the specific exception your Auth Service expects
                return new UsernameNotFoundException("User not found via user-service call.");
            }
            // Add a check for BadCredentialsException if the User Service returns 500 for that too
            // else if (responseBody.contains("Invalid password")) {
            //     return new BadCredentialsException("Invalid password via user-service call.");
            // }
        }

        // 2. Fall back to Spring's standard authentication exceptions if User Service returns 401/404 directly
        if (response.status() == 401) {
            return new BadCredentialsException("Authentication failed via user-service.");
        }

        // 3. Use the default decoder for all other errors (like connection issues, etc.)
        return defaultDecoder.decode(methodKey, response);
    }
}