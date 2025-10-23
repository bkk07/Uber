package com.uber.ratings.controller;

import com.uber.ratings.dto.ReviewRequest;
import com.uber.ratings.dto.ReviewResponse; // Import the new DTO
import com.uber.ratings.dto.ReviewUpdateRequest;
import com.uber.ratings.enums.ReviewType;
import com.uber.ratings.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    // Add a new Review

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest request) { // Change return type
        System.out.println("New Review Request Received: " + request);
        ReviewResponse newReviewResponse = reviewService.addReview(request);
        System.out.println("New Review Created: " + newReviewResponse);
        return new ResponseEntity<>(newReviewResponse, HttpStatus.CREATED);
    }
    // Fetching the Reviews By Reviews for the target for type(USER,DRIVER)
    @GetMapping("/{targetId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForTarget(
            @PathVariable Long targetId,
            @RequestParam(required = true) ReviewType type) { // type is now optional
        List<ReviewResponse> reviews = reviewService.getReviewsForTarget(targetId, type);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    // Fetching the Reviews by the DriverId
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForDriver(@PathVariable Long driverId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForTarget(driverId, ReviewType.USER_REVIEW);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    // Fetching the Reviews for the UserId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForTarget(userId, ReviewType.DRIVER_REVIEW);
        System.out.println("Review By the User:"+reviews);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    // Get Average Rating By the (userId or driverId) and the Type(User or Driver)
    @GetMapping("/{targetId}/average")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable Long targetId,
            @RequestParam ReviewType type) {
        Double averageRating = reviewService.getAverageRating(targetId, type);
        return new ResponseEntity<>(averageRating, HttpStatus.OK);
    }

    // Update the review
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewResponse updatedReview = reviewService.updateReview(id, request);
        return new ResponseEntity<>(updatedReview, HttpStatus.OK);
    }
}