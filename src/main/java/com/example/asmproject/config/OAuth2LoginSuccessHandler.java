package com.example.asmproject.config;

import com.example.asmproject.model.User;
import com.example.asmproject.service.AddressService;
import com.example.asmproject.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Handler xử lý sau khi đăng nhập Google OAuth2 thành công
 * Tự động đăng ký user nếu chưa có trong hệ thống hoặc đăng nhập nếu đã có
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Autowired
    @Lazy // Trì hoãn injection để tránh circular dependency
    private UserService userService;
    
    @Autowired
    @Lazy
    private AddressService addressService;
    
    /**
     * Xử lý sau khi đăng nhập OAuth2 thành công
     * 
     * Logic:
     * 1. Lấy thông tin từ OAuth2User (Google)
     * 2. Kiểm tra xem user đã có trong database chưa
     * 3. Nếu chưa có thì tự động đăng ký
     * 4. Nếu có rồi thì chỉ cần đăng nhập
     * 5. Tạo session cho user và redirect về trang chủ
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        // Lấy thông tin từ OAuth2User (Google)
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Lấy thông tin từ Google
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub"); // Google User ID
        
        // Kiểm tra và đăng ký/đăng nhập user
        try {
            // Gọi service để đăng ký hoặc đăng nhập
            // Service sẽ tự động:
            // - Tìm user theo providerId (nếu có thì đăng nhập)
            // - Nếu chưa có nhưng email đã tồn tại thì liên kết Google với email đó
            // - Nếu chưa có thì tạo user mới
            User user = userService.registerWithGoogle(email, name, providerId);
            
            // Tạo UserDetails cho Spring Security
            // Để có thể sử dụng user trong SecurityContext
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
            
            // Cập nhật Authentication với UserDetails từ database
            // Thay vì dùng OAuth2User, ta dùng UserDetails từ database để có đầy đủ thông tin
            Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
            );
            
            // Lưu vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            
            // Set session để giữ đăng nhập
            request.getSession().setAttribute("user", user);
            
            // Kiểm tra nếu user mới (chưa có phone hoặc chưa có address)
            boolean isNewUser = (user.getPhone() == null || user.getPhone().isEmpty()) 
                             || addressService.getUserAddresses(user.getId()).isEmpty();
            
            if (isNewUser) {
                // Redirect đến trang tài khoản để nhập thông tin
                getRedirectStrategy().sendRedirect(request, response, "/tai-khoan");
                return;
            }
            
        } catch (Exception e) {
            // Nếu có lỗi thì redirect về trang login với error
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth_failed");
            return;
        }
        
        // Redirect về trang chủ sau khi đăng nhập thành công
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
    
    /**
     * Lấy authorities (roles) của user
     */
    private Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
}

