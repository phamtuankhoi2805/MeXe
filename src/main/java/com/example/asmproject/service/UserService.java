package com.example.asmproject.service;

import com.example.asmproject.dto.OrderResponse;
import com.example.asmproject.dto.UserRequest;
import com.example.asmproject.dto.UserResponse;
import com.example.asmproject.model.Order;
import com.example.asmproject.model.User;
import com.example.asmproject.model.enums.UserStatus;
import com.example.asmproject.repository.UserRepository;
import com.example.asmproject.service.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý các logic nghiệp vụ liên quan đến User
 * Bao gồm: đăng ký, đăng nhập Google, xác thực email, quên mật khẩu, đổi mật khẩu
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * Đăng ký tài khoản mới bằng email và mật khẩu
     * 
     * @param email Email của người dùng (phải unique)
     * @param password Mật khẩu (sẽ được mã hóa bằng BCrypt)
     * @param fullName Họ và tên
     * @param phone Số điện thoại (optional)
     * @return User object đã được lưu vào database
     * @throws RuntimeException nếu email đã tồn tại
     */
    public User register(String email, String password, String fullName, String phone) {
        // Kiểm tra email đã tồn tại chưa
        // Tránh trường hợp đăng ký trùng email
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng. Vui lòng sử dụng email khác hoặc đăng nhập.");
        }
        
        // Tạo user mới
        User user = new User();
        user.setEmail(email);
        // Mã hóa mật khẩu bằng BCrypt trước khi lưu
        // BCrypt tự động thêm salt để bảo mật hơn
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(User.Role.USER); // Mặc định là USER, ADMIN chỉ được tạo thủ công
        user.setEnabled(true); // Cho phép đăng nhập ngay
        user.setEmailVerified(false); // Chưa xác thực email
        
        // Tạo token xác thực ngẫu nhiên (UUID)
        // Token này sẽ được gửi trong email để người dùng click vào xác thực
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        
        // Gửi email xác thực
        // Email sẽ chứa link với token để người dùng click vào
        // Link format: /api/auth/verify-email?token={verificationToken}
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        
        // Lưu user vào database và trả về
        return userRepository.save(user);
    }
    
    /**
     * Đăng ký hoặc đăng nhập bằng tài khoản Google OAuth
     * 
     * Logic:
     * 1. Nếu đã có user với providerId này -> trả về user hiện tại (đăng nhập)
     * 2. Nếu chưa có providerId nhưng email đã tồn tại -> liên kết Google với email đó
     * 3. Nếu chưa có cả providerId và email -> tạo tài khoản mới
     * 
     * @param email Email từ Google (đã được Google xác thực)
     * @param fullName Tên đầy đủ từ Google
     * @param providerId Google User ID (unique, không bao giờ thay đổi)
     * @return User object (đã tồn tại hoặc mới tạo)
     */
    public User registerWithGoogle(String email, String fullName, String providerId) {
        // Bước 1: Kiểm tra xem đã có user nào đăng nhập bằng Google với providerId này chưa
        // Nếu có thì đây là lần đăng nhập tiếp theo -> trả về user hiện tại
        Optional<User> existingUser = userRepository.findByProviderAndProviderId("google", providerId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Bước 2: Kiểm tra xem email này đã được dùng để đăng ký tài khoản thường chưa
        // Nếu có thì liên kết Google với tài khoản đó
        // Điều này cho phép người dùng đăng nhập bằng cả email/password hoặc Google
        Optional<User> existingEmail = userRepository.findByEmail(email);
        if (existingEmail.isPresent()) {
            User user = existingEmail.get();
            // Liên kết Google với tài khoản hiện tại
            user.setProvider("google");
            user.setProviderId(providerId);
            // Email từ Google đã được xác thực nên set emailVerified = true
            user.setEmailVerified(true);
            return userRepository.save(user);
        }
        
        // Bước 3: Chưa có tài khoản nào -> tạo mới
        // Vì đăng nhập bằng Google nên không cần password
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setProvider("google");
        user.setProviderId(providerId);
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        // Email từ Google đã được xác thực nên set emailVerified = true
        user.setEmailVerified(true);
        
        return userRepository.save(user);
    }
    
    /**
     * Xác thực email bằng token
     * Token được gửi trong email khi đăng ký
     * 
     * @param token Verification token từ link trong email
     * @return true nếu xác thực thành công, false nếu token không hợp lệ
     */
    public boolean verifyEmail(String token) {
        // Tìm user có token này
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Đánh dấu email đã được xác thực
            user.setEmailVerified(true);
            // Xóa token sau khi đã sử dụng (token chỉ dùng 1 lần)
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        
        // Token không tồn tại hoặc đã được sử dụng
        return false;
    }
    
    /**
     * Yêu cầu đặt lại mật khẩu
     * Tạo reset token và gửi email chứa link đặt lại mật khẩu
     * Token có hiệu lực 24 giờ
     * 
     * @param email Email của người dùng quên mật khẩu
     */
    public void requestPasswordReset(String email) {
        // Tìm user theo email
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Tạo reset token ngẫu nhiên (UUID)
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            
            // Set thời hạn token là 24 giờ kể từ bây giờ
            // Sau 24 giờ token sẽ hết hiệu lực
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
            
            // Lưu token vào database
            userRepository.save(user);
            
            // Gửi email chứa link đặt lại mật khẩu
            // Link format: /reset-password?token={resetToken}
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        }
        // Lưu ý: Không throw exception nếu email không tồn tại
        // Để tránh hacker biết được email nào có trong hệ thống (bảo mật)
    }
    
    /**
     * Đặt lại mật khẩu bằng reset token
     * Token phải còn hiệu lực (chưa quá 24 giờ)
     * 
     * @param token Reset token từ link trong email
     * @param newPassword Mật khẩu mới (sẽ được mã hóa)
     * @return true nếu đặt lại thành công, false nếu token không hợp lệ hoặc hết hạn
     */
    public boolean resetPassword(String token, String newPassword) {
        // Tìm user có token này
        Optional<User> userOpt = userRepository.findByResetToken(token);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Kiểm tra token còn hiệu lực không
            // So sánh thời hạn token với thời gian hiện tại
            if (user.getResetTokenExpiry() != null && 
                user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
                
                // Token còn hiệu lực -> đặt mật khẩu mới
                // Mã hóa mật khẩu mới bằng BCrypt
                user.setPassword(passwordEncoder.encode(newPassword));
                
                // Xóa token sau khi đã sử dụng (token chỉ dùng 1 lần)
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                
                userRepository.save(user);
                return true;
            }
            // Token đã hết hạn (quá 24 giờ)
        }
        
        // Token không tồn tại hoặc đã hết hạn
        return false;
    }
    
    /**
     * Đổi mật khẩu khi người dùng đã đăng nhập
     * Yêu cầu mật khẩu cũ để xác thực
     * 
     * @param userId ID của người dùng (lấy từ session hoặc JWT)
     * @param oldPassword Mật khẩu cũ (để xác thực)
     * @param newPassword Mật khẩu mới (sẽ được mã hóa)
     * @throws RuntimeException nếu user không tồn tại hoặc mật khẩu cũ sai
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // Tìm user theo ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // So sánh mật khẩu cũ với mật khẩu trong database
        // passwordEncoder.matches() sẽ tự động so sánh plain text với BCrypt hash
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng. Vui lòng thử lại.");
        }
        
        // Mật khẩu cũ đúng -> đổi sang mật khẩu mới
        // Mã hóa mật khẩu mới bằng BCrypt
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Cập nhật thông tin profile của user
     * 
     * @param userId ID của user
     * @param fullName Họ tên mới (optional)
     * @param phone Số điện thoại mới (optional)
     * @param avatar URL ảnh đại diện mới (optional)
     * @return User object đã được cập nhật
     * @throws RuntimeException nếu user không tồn tại
     */
    public User updateProfile(Long userId, String fullName, String phone, String avatar) {
        // Tìm user theo ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Chỉ cập nhật các field có giá trị (không null)
        // Cho phép cập nhật từng phần thông tin
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone);
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            user.setAvatar(avatar);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Tìm user theo email
     * 
     * @param email Email của user
     * @return Optional<User> - có thể empty nếu không tìm thấy
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Tìm user theo ID
     * 
     * @param id ID của user
     * @return Optional<User> - có thể empty nếu không tìm thấy
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Đếm tổng số user trong hệ thống (chỉ USER role, không tính ADMIN)
     * 
     * @return Tổng số user
     */
    public long getTotalUsers() {
        return userRepository.countUsers();
    }
    
    // New methods for API
    public Page<UserResponse> searchUsers(String keyword, UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, status, pageable);
        return users.map(this::toResponse);
    }
    
    public UserResponse create(UserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.HOAT_DONG);
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(false);
        
        user = userRepository.save(user);
        return toResponse(user);
    }
    
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        user = userRepository.save(user);
        return toResponse(user);
    }
    
    public void deactivate(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        user.setStatus(UserStatus.TAM_KHOA);
        user.setEnabled(false);
        userRepository.save(user);
    }
    
    public List<OrderResponse> getPurchaseHistory(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        List<Order> orders = user.getOrders();
        return orders.stream()
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}

