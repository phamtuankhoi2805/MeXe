package com.example.asmproject.service;

import com.example.asmproject.model.User;
import com.example.asmproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    public User register(String email, String password, String fullName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        
        return userRepository.save(user);
    }
    
    public User registerWithGoogle(String email, String fullName, String providerId) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId("google", providerId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        Optional<User> existingEmail = userRepository.findByEmail(email);
        if (existingEmail.isPresent()) {
            User user = existingEmail.get();
            user.setProvider("google");
            user.setProviderId(providerId);
            user.setEmailVerified(true);
            return userRepository.save(user);
        }
        
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setProvider("google");
        user.setProviderId(providerId);
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(true);
        
        return userRepository.save(user);
    }
    
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setResetToken(UUID.randomUUID().toString());
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getResetToken());
        }
    }
    
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getResetTokenExpiry() != null && user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public User updateProfile(Long userId, String fullName, String phone, String avatar) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        if (avatar != null) user.setAvatar(avatar);
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public long getTotalUsers() {
        return userRepository.countUsers();
    }
}

