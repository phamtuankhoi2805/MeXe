package com.example.asmproject.service;

import com.example.asmproject.model.Order;
import com.example.asmproject.model.Product;
import com.example.asmproject.model.Review;
import com.example.asmproject.model.User;
import com.example.asmproject.repository.OrderRepository;
import com.example.asmproject.repository.ProductRepository;
import com.example.asmproject.repository.ReviewRepository;
import com.example.asmproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public Review createReview(Long userId, Long orderId, Long productId, 
                              Integer rating, String comment, String images) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này");
        }
        
        if (order.getOrderStatus() != Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Chỉ có thể đánh giá đơn hàng đã giao thành công");
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        // Check if already reviewed
        Optional<Review> existingReview = reviewRepository
            .findByUserIdAndOrderIdAndProductId(userId, orderId, productId);
        
        if (existingReview.isPresent()) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này trong đơn hàng này");
        }
        
        Review review = new Review();
        review.setUser(user);
        review.setOrder(order);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setImages(images);
        
        return reviewRepository.save(review);
    }
    
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    public Page<Review> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }
    
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }
    
    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }
}

