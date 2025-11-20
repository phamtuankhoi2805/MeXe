package com.example.asmproject.controller.api;

import com.example.asmproject.model.User;
import com.example.asmproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            
            User user = userService.register(email, password, fullName, phone);
            response.put("success", true);
            response.put("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            userService.requestPasswordReset(email);
            response.put("success", true);
            response.put("message", "Email đặt lại mật khẩu đã được gửi.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            boolean success = userService.resetPassword(token, newPassword);
            if (success) {
                response.put("success", true);
                response.put("message", "Đặt lại mật khẩu thành công.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Token không hợp lệ hoặc đã hết hạn.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request,
                                                              @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            userService.changePassword(userId, oldPassword, newPassword);
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        boolean success = userService.verifyEmail(token);
        if (success) {
            response.put("success", true);
            response.put("message", "Xác thực email thành công.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Token xác thực không hợp lệ.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

