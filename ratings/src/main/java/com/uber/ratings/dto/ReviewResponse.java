package com.uber.ratings.dto;

import com.uber.ratings.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Or any other relevant timestamp if needed

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long reviewerId;
    private Long targetId;
    private Integer rating;
    private String comment;
    private String rideId;
    private ReviewType type;
    private LocalDateTime updatedAt;
}