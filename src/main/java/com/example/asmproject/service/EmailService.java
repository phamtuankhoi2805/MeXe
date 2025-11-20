package com.example.asmproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public void sendVerificationEmail(String email, String token) {
        // TODO: Implement email sending with Spring Mail
        // For now, just log the verification link
        String verificationLink = baseUrl + "/verify-email?token=" + token;
        System.out.println("Verification link for " + email + ": " + verificationLink);
    }
    
    public void sendPasswordResetEmail(String email, String token) {
        // TODO: Implement email sending with Spring Mail
        String resetLink = baseUrl + "/reset-password?token=" + token;
        System.out.println("Password reset link for " + email + ": " + resetLink);
    }
}

