package com.uber.adminservice.exception;
import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String path,
        int status,
        LocalDateTime timestamp
) {}
