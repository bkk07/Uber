package com.uber.ratings.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;
    private String description;
//
//    public ErrorDetails(LocalDateTime timestamp, String message, String details,String description) {
//        this.timestamp = timestamp;
//        this.message = message;
//        this.details = details;
//        this.description = description;
//    }
}