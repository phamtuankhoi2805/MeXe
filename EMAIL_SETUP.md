# Hướng dẫn cấu hình Email với Spring Mail

## 1. Cấu hình Gmail (Khuyến nghị)

### Bước 1: Tạo App Password cho Gmail

1. Đăng nhập vào tài khoản Google của bạn
2. Truy cập: https://myaccount.google.com/security
3. Bật **2-Step Verification** (Xác thực 2 bước) nếu chưa bật
4. Sau khi bật 2-Step Verification, truy cập: https://myaccount.google.com/apppasswords
5. Chọn **Mail** và **Other (Custom name)**, nhập tên như "VinFast App"
6. Click **Generate** để tạo App Password (16 ký tự)
7. Copy App Password này (sẽ dùng trong `application.properties`)

### Bước 2: Cập nhật application.properties

Mở file `src/main/resources/application.properties` và cập nhật:

```properties
# Spring Mail Configuration - Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-digit-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email sender configuration
mail.from.email=your-email@gmail.com
mail.from.name=VinFast Store
```

**Lưu ý:** 
- `spring.mail.username`: Email Gmail của bạn
- `spring.mail.password`: App Password 16 ký tự (KHÔNG phải mật khẩu Gmail thông thường)
- `mail.from.email`: Có thể dùng email khác, nhưng phải là email hợp lệ

## 2. Cấu hình Outlook/Hotmail

```properties
# Spring Mail Configuration - Outlook
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

## 3. Cấu hình SMTP Server khác

Nếu bạn có SMTP server riêng (ví dụ: SendGrid, Mailgun, AWS SES):

```properties
# Spring Mail Configuration - Custom SMTP
spring.mail.host=smtp.your-provider.com
spring.mail.port=587
spring.mail.username=your-username
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 4. Test Email

Sau khi cấu hình, khởi động lại ứng dụng và thử đăng ký tài khoản mới. Email xác thực sẽ được gửi đến email người dùng.

## 5. Troubleshooting

### Lỗi: "Authentication failed"
- Kiểm tra lại App Password (Gmail) hoặc mật khẩu
- Đảm bảo đã bật 2-Step Verification cho Gmail

### Lỗi: "Connection timeout"
- Kiểm tra firewall/antivirus có chặn port 587 không
- Thử đổi port sang 465 với SSL:
  ```properties
  spring.mail.port=465
  spring.mail.properties.mail.smtp.ssl.enable=true
  ```

### Email không được gửi nhưng không có lỗi
- Kiểm tra thư mục Spam/Junk
- Kiểm tra console log để xem có lỗi gì không
- Nếu `mailSender` là `null`, có nghĩa là Spring Mail chưa được cấu hình đúng

## 6. Fallback Mode

Nếu không cấu hình email, hệ thống sẽ tự động fallback về chế độ log ra console. Mã xác nhận sẽ được in ra console để test.

