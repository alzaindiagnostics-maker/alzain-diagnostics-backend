package com.alzain.controller;

import com.alzain.entity.Review;
import com.alzain.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/reviews")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PublicReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApprovedReviews() {
        return ResponseEntity.ok(reviewService.getApprovedReviewsSummary());
    }

    @PostMapping
    public ResponseEntity<Review> submitReview(@Valid @RequestBody Review review) {
        Review saved = reviewService.submitReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
