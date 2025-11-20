package com.example.asmproject.util;

import com.example.asmproject.model.User;
import com.example.asmproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class để làm việc với Spring Security
 * Hỗ trợ lấy thông tin user hiện tại từ SecurityContext
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Component
public class SecurityUtil {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy email của user hiện tại từ SecurityContext
     * 
     * @return Email của user hiện tại, null nếu chưa đăng nhập
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        
        return null;
    }
    
    /**
     * Lấy User object của user hiện tại từ SecurityContext
     * 
     * @return Optional<User> - có thể empty nếu chưa đăng nhập
     */
    public Optional<User> getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email != null) {
            return userRepository.findByEmail(email);
        }
        return Optional.empty();
    }
    
    /**
     * Lấy ID của user hiện tại
     * 
     * @return ID của user hiện tại, null nếu chưa đăng nhập
     */
    public Long getCurrentUserId() {
        Optional<User> user = getCurrentUser();
        return user.map(User::getId).orElse(null);
    }
    
    /**
     * Kiểm tra user hiện tại có role ADMIN không
     * 
     * @return true nếu user hiện tại là ADMIN
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        
        return false;
    }
    
    /**
     * Kiểm tra user hiện tại có đang đăng nhập không
     * 
     * @return true nếu đã đăng nhập
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser");
    }
    
    /**
     * Kiểm tra user hiện tại có phải là owner của resource không (theo userId)
     * 
     * @param userId ID của user sở hữu resource
     * @return true nếu user hiện tại là owner hoặc là ADMIN
     */
    public boolean isOwnerOrAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        
        // Admin có thể truy cập tất cả
        if (isAdmin()) {
            return true;
        }
        
        // Kiểm tra user hiện tại có phải owner không
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }
}

