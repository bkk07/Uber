package com.uber.ratings.dto;

import com.uber.ratings.enums.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotNull(message = "Reviewer ID cannot be null")
    private Long reviewerId;
    @NotNull(message = "Target ID cannot be null")
    private Long targetId;
    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;
    @NotNull(message = "Ride ID cannot be null")
    private String rideId;
    @NotNull(message = "Review type cannot be null")
    private ReviewType type;
}