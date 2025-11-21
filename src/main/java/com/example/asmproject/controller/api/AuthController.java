package com.example.asmproject.controller.api;

import com.example.asmproject.model.User;
import com.example.asmproject.service.UserService;
import com.example.asmproject.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý các API liên quan đến xác thực người dùng
 * Bao gồm: đăng ký, đăng nhập, quên mật khẩu, đổi mật khẩu, xác thực email
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    /**
     * API đăng ký tài khoản mới bằng email
     * Người dùng cung cấp email, mật khẩu, họ tên và số điện thoại
     * Hệ thống sẽ gửi email xác thực sau khi đăng ký thành công
     * 
     * @param request Chứa các thông tin: email, password, fullName, phone
     * @return JSON response với thông báo kết quả và userId nếu thành công
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin từ request body
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            
            // Kiểm tra dữ liệu đầu vào với thông báo chi tiết
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống.");
                response.put("field", "email");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate email format
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!email.matches(emailRegex)) {
                response.put("success", false);
                response.put("message", "Email không hợp lệ. Vui lòng nhập đúng định dạng email (ví dụ: user@example.com).");
                response.put("field", "email");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (fullName == null || fullName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Họ và tên không được để trống.");
                response.put("field", "fullName");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (fullName.trim().length() < 2) {
                response.put("success", false);
                response.put("message", "Họ và tên phải có ít nhất 2 ký tự.");
                response.put("field", "fullName");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (password == null || password.isEmpty()) {
                response.put("success", false);
                response.put("message", "Mật khẩu không được để trống.");
                response.put("field", "password");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate password strength
            if (password.length() < 6) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 6 ký tự.");
                response.put("field", "password");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (password.length() > 50) {
                response.put("success", false);
                response.put("message", "Mật khẩu không được vượt quá 50 ký tự.");
                response.put("field", "password");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate phone (optional but if provided, should be valid)
            if (phone != null && !phone.trim().isEmpty()) {
                String phoneRegex = "^[0-9]{10,11}$";
                String phoneDigits = phone.replaceAll("[^0-9]", "");
                if (!phoneDigits.matches(phoneRegex)) {
                    response.put("success", false);
                    response.put("message", "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại từ 10-11 chữ số.");
                    response.put("field", "phone");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Gọi service để đăng ký tài khoản
            // Service sẽ tự động:
            // - Mã hóa mật khẩu bằng BCrypt
            // - Tạo verification token
            // - Gửi email xác thực
            User user = userService.register(email.trim(), password, fullName.trim(), phone != null ? phone.trim() : null);
            
            // Trả về kết quả thành công
            response.put("success", true);
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã xác nhận 6 số.");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Xử lý lỗi từ service (ví dụ: email đã tồn tại)
            response.put("success", false);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Email đã được sử dụng")) {
                response.put("message", "Email này đã được sử dụng. Vui lòng sử dụng email khác hoặc đăng nhập.");
                response.put("field", "email");
            } else {
                response.put("message", errorMessage != null ? errorMessage : "Đăng ký thất bại. Vui lòng thử lại.");
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // Xử lý lỗi không mong đợi
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại sau.");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * API đăng ký/đăng nhập bằng tài khoản Google OAuth
     * Người dùng cung cấp thông tin từ Google (email, fullName, providerId)
     * Hệ thống sẽ tự động tạo tài khoản nếu chưa tồn tại hoặc đăng nhập nếu đã có
     * 
     * @param request Chứa các thông tin: email, fullName, providerId (Google user ID)
     * @return JSON response với thông tin user và token nếu thành công
     */
    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> registerWithGoogle(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin từ Google OAuth
            String email = request.get("email");
            String fullName = request.get("fullName");
            String providerId = request.get("providerId"); // Google user ID
            
            // Validate dữ liệu
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (providerId == null || providerId.isEmpty()) {
                response.put("success", false);
                response.put("message", "Provider ID không hợp lệ.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Đăng ký hoặc đăng nhập bằng Google
            // Service sẽ tự động:
            // - Kiểm tra xem đã có tài khoản với providerId này chưa
            // - Nếu có thì trả về user hiện tại
            // - Nếu chưa có nhưng email đã tồn tại thì liên kết Google với email đó
            // - Nếu chưa có thì tạo tài khoản mới (email đã được Google xác thực)
            User user = userService.registerWithGoogle(email, fullName, providerId);
            
            response.put("success", true);
            response.put("message", "Đăng nhập Google thành công.");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("emailVerified", user.getEmailVerified());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API quên mật khẩu
     * Người dùng nhập email, hệ thống sẽ gửi link đặt lại mật khẩu qua email
     * Link này có thời hạn 24 giờ
     * 
     * @param request Chứa email của người dùng
     * @return JSON response với thông báo đã gửi email (bảo mật: không tiết lộ email có tồn tại hay không)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gọi service để yêu cầu đặt lại mật khẩu
            // Service sẽ:
            // - Tìm user theo email
            // - Tạo reset token ngẫu nhiên
            // - Set thời hạn token là 24 giờ
            // - Gửi email chứa link đặt lại mật khẩu
            // Lưu ý: Không tiết lộ email có tồn tại hay không (bảo mật)
            userService.requestPasswordReset(email);
            
            // Luôn trả về thành công để bảo mật (không cho hacker biết email có tồn tại)
            response.put("success", true);
            response.put("message", "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API đặt lại mật khẩu sau khi nhận được token từ email
     * Người dùng nhập token và mật khẩu mới
     * Token chỉ có hiệu lực trong 24 giờ
     * 
     * @param request Chứa token và newPassword
     * @return JSON response với thông báo kết quả
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            // Validate dữ liệu
            if (token == null || token.isEmpty()) {
                response.put("success", false);
                response.put("message", "Token không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword == null || newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới phải có ít nhất 6 ký tự.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gọi service để đặt lại mật khẩu
            // Service sẽ:
            // - Tìm user theo token
            // - Kiểm tra token còn hiệu lực không (chưa quá 24 giờ)
            // - Mã hóa mật khẩu mới
            // - Xóa token và thời hạn token
            boolean success = userService.resetPassword(token, newPassword);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Token không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API đổi mật khẩu khi người dùng đã đăng nhập
     * Yêu cầu mật khẩu cũ để xác thực, sau đó đặt mật khẩu mới
     * 
     * Chỉ user đã đăng nhập mới có thể đổi mật khẩu của chính mình
     * 
     * @param request Chứa oldPassword và newPassword
     * @param userId ID của người dùng (optional, nếu không có thì dùng user hiện tại)
     * @return JSON response với thông báo kết quả
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request,
                                                              @RequestParam(required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            // Validate dữ liệu
            if (oldPassword == null || oldPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "Mật khẩu cũ không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword == null || newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới phải có ít nhất 6 ký tự.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (oldPassword.equals(newPassword)) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới phải khác mật khẩu cũ.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Nếu không có userId thì lấy từ user hiện tại
            // Đảm bảo user chỉ có thể đổi mật khẩu của chính mình
            if (userId == null) {
                userId = securityUtil.getCurrentUserId();
                if (userId == null) {
                    response.put("success", false);
                    response.put("message", "Bạn phải đăng nhập để đổi mật khẩu.");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                // Kiểm tra user chỉ có thể đổi mật khẩu của chính mình (trừ admin)
                Long currentUserId = securityUtil.getCurrentUserId();
                if (!securityUtil.isAdmin() && !userId.equals(currentUserId)) {
                    response.put("success", false);
                    response.put("message", "Bạn không có quyền đổi mật khẩu của người khác.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Gọi service để đổi mật khẩu
            // Service sẽ:
            // - Tìm user theo userId
            // - So sánh mật khẩu cũ với mật khẩu trong database (đã mã hóa)
            // - Nếu đúng thì mã hóa mật khẩu mới và lưu
            // - Nếu sai thì throw exception
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
    
    /**
     * API xác thực email
     * Người dùng click vào link trong email xác thực
     * Link này chứa token được gửi khi đăng ký
     * 
     * @param token Token xác thực được gửi trong email
     * @return JSON response với thông báo kết quả
     */
    /**
     * API xác thực email bằng mã xác nhận 6 số
     * POST /api/auth/verify-email
     * Body: { "email": "user@example.com", "code": "123456" }
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (code == null || code.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã xác nhận không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Gọi service để xác thực email bằng mã 6 số
        boolean success = userService.verifyEmail(code, email);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Xác thực email thành công. Bạn có thể đăng nhập ngay bây giờ.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Mã xác nhận không hợp lệ hoặc đã được sử dụng.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API xác thực email bằng token (giữ lại để tương thích)
     * GET /api/auth/verify-email?token=xxx
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmailByToken(@RequestParam(required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        if (token == null || token.isEmpty()) {
            response.put("success", false);
            response.put("message", "Token xác thực không hợp lệ.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Gọi service để xác thực email bằng token
        boolean success = userService.verifyEmail(token);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Xác thực email thành công. Bạn có thể đăng nhập ngay bây giờ.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Token xác thực không hợp lệ hoặc đã được sử dụng.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API gửi lại mã xác nhận email
     * POST /api/auth/resend-verification
     * Body: { "email": "user@example.com" }
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Gọi service để gửi lại mã xác nhận
        boolean success = userService.resendVerificationCode(email);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Mã xác nhận mới đã được gửi đến email của bạn.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Email không tồn tại hoặc đã được xác thực.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

