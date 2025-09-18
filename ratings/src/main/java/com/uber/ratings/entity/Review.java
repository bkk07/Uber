package com.uber.ratings.entity;

import com.uber.ratings.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long reviewerId;
    private Long targetId; // ID of the user or driver being reviewed
    private Integer rating; // Rating from 1 to 5
    private String comment;
    private String rideId; // The ride associated with this review
    @Enumerated(EnumType.STRING)
    private ReviewType type; // DRIVER_REVIEW or USER_REVIEW

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}