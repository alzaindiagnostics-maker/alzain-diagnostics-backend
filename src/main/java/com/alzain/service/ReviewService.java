package com.alzain.service;

import com.alzain.entity.Review;
import com.alzain.exception.BadRequestException;
import com.alzain.exception.ResourceNotFoundException;
import com.alzain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public Review submitReview(Review review) {
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5 stars");
        }

        if (review.getCustomerName() == null || review.getCustomerName().trim().isEmpty()) {
            throw new BadRequestException("Customer name is required");
        }

        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            throw new BadRequestException("Review text is required");
        }

        review.setCustomerName(review.getCustomerName().trim());
        review.setReviewText(review.getReviewText().trim());
        review.setStatus("PENDING");

        Review savedReview = reviewRepository.save(review);
        log.info("New review submitted ID: {} by customer: {}. Marked as PENDING.", savedReview.getId(), savedReview.getCustomerName());

        // Notify admin via Gmail SMTP
        try {
            emailNotificationService.sendNewReviewNotification(savedReview);
        } catch (Exception e) {
            log.warn("Failed to dispatch review notification email: {}", e.getMessage());
        }

        return savedReview;
    }

    public List<Review> getApprovedReviews() {
        return reviewRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
    }

    public Map<String, Object> getApprovedReviewsSummary() {
        List<Review> approved = getApprovedReviews();
        double avgRating = approved.stream().mapToInt(Review::getRating).average().orElse(5.0);

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", approved);
        response.put("totalReviews", approved.size());
        response.put("averageRating", Math.round(avgRating * 10.0) / 10.0);

        return response;
    }

    public List<Review> getAllReviewsForAdmin() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Review updateReviewStatus(Long reviewId, String newStatus) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        String statusUpper = newStatus != null ? newStatus.trim().toUpperCase() : "";
        if (!"APPROVED".equals(statusUpper) && !"REJECTED".equals(statusUpper) && !"PENDING".equals(statusUpper)) {
            throw new BadRequestException("Invalid status value. Must be PENDING, APPROVED, or REJECTED.");
        }

        review.setStatus(statusUpper);
        Review updated = reviewRepository.save(review);
        log.info("Review ID: {} status updated to {}", reviewId, statusUpper);
        return updated;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
        log.info("Review ID: {} deleted by admin.", reviewId);
    }
}
