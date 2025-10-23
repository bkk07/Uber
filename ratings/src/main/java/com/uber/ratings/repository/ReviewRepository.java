package com.uber.ratings.repository;

import com.uber.ratings.entity.Review;
import com.uber.ratings.enums.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetId(Long targetId);
    List<Review> findByTargetIdAndType(Long targetId, ReviewType type);
    List<Review> findByReviewerId(Long targetId);
    List<Review> findByReviewerIdAndType(Long targetId, ReviewType type);
}