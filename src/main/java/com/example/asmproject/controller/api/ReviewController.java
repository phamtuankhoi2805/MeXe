package com.example.asmproject.controller.api;

import com.example.asmproject.model.Review;
import com.example.asmproject.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long orderId = Long.valueOf(request.get("orderId").toString());
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer rating = Integer.valueOf(request.get("rating").toString());
            String comment = (String) request.get("comment");
            String images = (String) request.get("images");
            
            Review review = reviewService.createReview(userId, orderId, productId, rating, comment, images);
            response.put("success", true);
            response.put("message", "Đánh giá thành công.");
            response.put("review", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/product/{productId}/page")
    public ResponseEntity<Page<Review>> getProductReviewsPage(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(@PathVariable Long productId) {
        Map<String, Object> stats = new HashMap<>();
        Double averageRating = reviewService.getAverageRating(productId);
        long totalReviews = reviewService.getReviewCount(productId);
        
        stats.put("averageRating", averageRating != null ? averageRating : 0.0);
        stats.put("totalReviews", totalReviews);
        return ResponseEntity.ok(stats);
    }
}

