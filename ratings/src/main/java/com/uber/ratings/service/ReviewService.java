package com.uber.ratings.service;

import com.uber.ratings.dto.ReviewRequest;
import com.uber.ratings.dto.ReviewResponse;
import com.uber.ratings.dto.ReviewUpdateRequest;
import com.uber.ratings.entity.Review;
import com.uber.ratings.enums.ReviewType;
import com.uber.ratings.exception.ResourceNotFoundException;
import com.uber.ratings.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewResponse addReview(ReviewRequest request) {
        Review review = new Review();
        review.setReviewerId(request.getReviewerId());
        review.setTargetId(request.getTargetId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setRideId(request.getRideId());
        review.setType(request.getType());
        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    public List<ReviewResponse> getReviewsForTarget(Long targetId, ReviewType type) {
        List<Review> reviews;
        if (type != null) {
            reviews = reviewRepository.findByTargetIdAndType(targetId, type);
        } else {
            reviews = reviewRepository.findByTargetId(targetId);
        }

        if (reviews.isEmpty()) {
            String message = (type != null) ?
                    "No " + type.name().toLowerCase().replace("_", " ") + " reviews found for target ID: " + targetId :
                    "No reviews found for target ID: " + targetId;
            throw new ResourceNotFoundException(message);
        }
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(Long targetId, ReviewType type) {
        List<Review> reviews = reviewRepository.findByTargetIdAndType(targetId, type);
        OptionalDouble average = reviews.stream()
                .mapToInt(Review::getRating)
                .average();
        return average.orElse(0.0);
        // Here We added 0 because no ratings found for the Rider or User
    }

    public ReviewResponse updateReview(Long id, ReviewUpdateRequest request) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        existingReview.setRating(request.getRating());
        existingReview.setComment(request.getComment());
        existingReview.setType(request.getType());
        Review updatedReview = reviewRepository.save(existingReview);
        return mapToReviewResponse(updatedReview);
    }

    // This is a Helper methods to map the Review to the reviewResponse
    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerId(review.getReviewerId())
                .comment(review.getComment())
                .targetId(review.getTargetId())
                .rating(review.getRating())
                .rideId(review.getRideId())
                .type(review.getType())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

}