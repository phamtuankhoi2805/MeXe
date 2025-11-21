package com.example.asmproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${mail.from.email:noreply@vinfast.com}")
    private String fromEmail;

    @Value("${mail.from.name:VinFast Store}")
    private String fromName;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Gửi email xác thực với mã xác nhận 6 số
     * 
     * @param email            Email người nhận
     * @param fullName         Tên người dùng
     * @param verificationCode Mã xác nhận 6 số
     */
    public void sendVerificationEmail(String email, String fullName, String verificationCode) {
        try {
            if (mailSender != null) {
                // Gửi email HTML thực sự
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail, fromName);
                helper.setTo(email);
                helper.setSubject("Xác thực email đăng ký tài khoản VinFast");

                String htmlContent = buildVerificationEmailHtml(fullName, verificationCode);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                System.out.println("Email xác thực đã được gửi thành công đến: " + email);
            } else {
                // Fallback: log ra console nếu chưa cấu hình mail
                System.out.println("========================================");
                System.out.println("EMAIL XÁC THỰC CHO: " + email);
                System.out.println("Tên: " + fullName);
                System.out.println("Mã xác nhận: " + verificationCode);
                System.out.println("Vui lòng nhập mã này vào trang xác thực: " + baseUrl + "/verify-email");
                System.out.println("========================================");
            }
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email xác thực: " + e.getMessage());
            e.printStackTrace();
            // Fallback: log ra console
            System.out.println("========================================");
            System.out.println("EMAIL XÁC THỰC CHO: " + email);
            System.out.println("Tên: " + fullName);
            System.out.println("Mã xác nhận: " + verificationCode);
            System.out.println("========================================");
        } catch (Exception e) {
            System.err.println("Lỗi không mong đợi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tạo nội dung email HTML cho xác thực
     */
    private String buildVerificationEmailHtml(String fullName, String verificationCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
                +
                ".content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".code-box { background: white; border: 2px dashed #2563eb; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }"
                +
                ".code { font-size: 32px; font-weight: bold; color: #2563eb; letter-spacing: 5px; }" +
                ".footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Xác thực email đăng ký</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + fullName + "</strong>,</p>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản VinFast. Để hoàn tất đăng ký, vui lòng sử dụng mã xác nhận sau:</p>"
                +
                "<div class='code-box'>" +
                "<div class='code'>" + verificationCode + "</div>" +
                "</div>" +
                "<p>Mã xác nhận này có hiệu lực trong 24 giờ. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>" +
                "<p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>" +
                "<p>Trân trọng,<br><strong>Đội ngũ VinFast</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Gửi email đặt lại mật khẩu
     * 
     * @param email Email người nhận
     * @param token Reset token
     */
    public void sendPasswordResetEmail(String email, String token) {
        try {
            if (mailSender != null) {
                // Gửi email HTML thực sự
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail, fromName);
                helper.setTo(email);
                helper.setSubject("Đặt lại mật khẩu tài khoản VinFast");

                String resetLink = baseUrl + "/reset-password?token=" + token;
                String htmlContent = buildPasswordResetEmailHtml(resetLink);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                System.out.println("Email đặt lại mật khẩu đã được gửi thành công đến: " + email);
            } else {
                // Fallback: log ra console
                String resetLink = baseUrl + "/reset-password?token=" + token;
                System.out.println("Password reset link for " + email + ": " + resetLink);
            }
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email đặt lại mật khẩu: " + e.getMessage());
            e.printStackTrace();
            // Fallback: log ra console
            String resetLink = baseUrl + "/reset-password?token=" + token;
            System.out.println("Password reset link for " + email + ": " + resetLink);
        } catch (Exception e) {
            System.err.println("Lỗi không mong đợi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tạo nội dung email HTML cho đặt lại mật khẩu
     */
    private String buildPasswordResetEmailHtml(String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
                +
                ".content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".button { display: inline-block; background: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin: 20px 0; }"
                +
                ".button:hover { background: #1e40af; }" +
                ".footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }" +
                ".warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px; margin: 20px 0; border-radius: 4px; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Đặt lại mật khẩu</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào,</p>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản VinFast của mình.</p>" +
                "<p style='text-align: center;'>" +
                "<a href='" + resetLink + "' class='button'>Đặt lại mật khẩu</a>" +
                "</p>" +
                "<p>Hoặc copy và paste link sau vào trình duyệt:</p>" +
                "<p style='word-break: break-all; color: #2563eb;'>" + resetLink + "</p>" +
                "<div class='warning'>" +
                "<p><strong>Lưu ý:</strong> Link này có hiệu lực trong 24 giờ. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                +
                "</div>" +
                "<p>Trân trọng,<br><strong>Đội ngũ VinFast</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
