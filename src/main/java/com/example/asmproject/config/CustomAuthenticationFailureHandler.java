package com.example.asmproject.config;

import com.example.asmproject.model.User;
import com.example.asmproject.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Handler xử lý lỗi đăng nhập
 * Phân tích nguyên nhân lỗi và redirect với thông báo phù hợp
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String email = request.getParameter("username");
        String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại.";
        String errorType = "general";
        
        // Kiểm tra các trường hợp lỗi cụ thể
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByEmail(email.trim());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Kiểm tra tài khoản bị vô hiệu hóa
                if (!user.getEnabled()) {
                    errorMessage = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.";
                    errorType = "account_disabled";
                }
                // Kiểm tra email chưa được xác thực (chỉ cho đăng nhập bằng email/password)
                else if (user.getProvider() == null && !user.getEmailVerified()) {
                    errorMessage = "Email chưa được xác thực. Vui lòng kiểm tra email và nhập mã xác nhận 6 số.";
                    errorType = "email_not_verified";
                }
                // Kiểm tra mật khẩu sai
                else if (exception.getMessage() != null && 
                        (exception.getMessage().contains("Bad credentials") || 
                         exception.getMessage().contains("Mật khẩu"))) {
                    errorMessage = "Email hoặc mật khẩu không đúng. Vui lòng kiểm tra lại.";
                    errorType = "bad_credentials";
                }
            } else {
                // Email không tồn tại
                errorMessage = "Email hoặc mật khẩu không đúng. Vui lòng kiểm tra lại.";
                errorType = "bad_credentials";
            }
        }
        
        // Kiểm tra các exception cụ thể
        if (exception.getMessage() != null) {
            String msg = exception.getMessage();
            if (msg.contains("Email chưa được xác thực")) {
                errorMessage = "Email chưa được xác thực. Vui lòng kiểm tra email và nhập mã xác nhận 6 số.";
                errorType = "email_not_verified";
            } else if (msg.contains("không tồn tại") || msg.contains("vô hiệu hóa")) {
                errorMessage = "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa.";
                errorType = "account_disabled";
            }
        }
        
        // Encode error message để truyền qua URL
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String redirectUrl = "/login?error=" + errorType + "&message=" + encodedMessage;
        
        response.sendRedirect(redirectUrl);
    }
}

