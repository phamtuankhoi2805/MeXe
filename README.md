# VinFast Motorbike E-Commerce System

Hệ thống bán hàng trực tuyến xe máy điện VinFast được xây dựng với Spring Boot và SQL Server.

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.3.1, Java 17
- **Database**: SQL Server
- **Security**: Spring Security với OAuth2 (Google)
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Build Tool**: Maven

## Cấu trúc Project

```
asm-project/
├── src/
│   ├── main/
│   │   ├── java/com/example/asmproject/
│   │   │   ├── config/          # Cấu hình (Security, etc.)
│   │   │   ├── controller/      # Controllers (Web & REST API)
│   │   │   ├── model/           # Entity classes
│   │   │   ├── repository/      # JPA Repositories
│   │   │   ├── service/         # Business logic
│   │   │   └── AsmProjectApplication.java
│   │   └── resources/
│   │       ├── database/        # SQL scripts
│   │       ├── static/          # CSS, JS, Images
│   │       ├── templates/       # Thymeleaf templates
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## Cài đặt và Chạy

### Yêu cầu

1. Java 17 hoặc cao hơn
2. Maven 3.6+
3. SQL Server (2019 hoặc cao hơn)
4. IDE (IntelliJ IDEA, Eclipse, hoặc VS Code)

### Bước 1: Tạo Database

1. Mở SQL Server Management Studio (SSMS)
2. Chạy script trong `src/main/resources/database/schema.sql` để tạo database và các bảng
3. Hoặc đảm bảo SQL Server đang chạy và để Spring Boot tự động tạo schema (ddl-auto=update)

### Bước 2: Cấu hình Database

Mở file `src/main/resources/application.properties` và cập nhật thông tin kết nối:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=VinFastMotorbikeDB;encrypt=true;trustServerCertificate=true;
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD
```

### Bước 3: Cấu hình Google OAuth (Tùy chọn)

1. Tạo Google OAuth 2.0 Client ID tại [Google Cloud Console](https://console.cloud.google.com/)
2. Cập nhật trong `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

### Bước 4: Build và Chạy

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## Tài khoản mặc định

**Admin:**
- Email: `admin@vinfast.com`
- Password: `admin123`

## Các chức năng chính

### Người dùng

- ✅ Đăng ký/Đăng nhập (Email và Google OAuth)
- ✅ Đổi mật khẩu
- ✅ Quên mật khẩu và đặt lại mật khẩu
- ✅ Tìm kiếm sản phẩm
- ✅ Quản lý giỏ hàng (Đồng bộ trên các thiết bị)
- ✅ Lấy mã giảm giá
- ✅ Quản lý sổ địa chỉ (Tối đa 4 địa chỉ)
- ✅ Đặt hàng
- ✅ Thanh toán đơn hàng
- ✅ Đánh giá đơn hàng
- ✅ Mua lại đơn hàng
- ✅ Quản lý lịch sử đơn hàng

### Admin

- ✅ CRUD: Đơn hàng, Sản phẩm, Màu sắc, Danh mục, Thương hiệu, Voucher
- ✅ Xem thống kê về trạng thái kinh doanh
- ✅ Quản lý người dùng
- ✅ Xem lịch sử mua hàng của khách hàng
- ✅ Xuất báo cáo đơn hàng (TODO: Implement Excel export)
- ✅ Tìm kiếm và lọc
- ✅ Tạo đơn hàng với giao hàng nhanh
- ✅ Xem trạng thái đơn hàng giao hàng nhanh

## API Endpoints

### User APIs

- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/forgot-password` - Quên mật khẩu
- `POST /api/auth/reset-password` - Đặt lại mật khẩu
- `POST /api/auth/change-password` - Đổi mật khẩu
- `GET /api/products` - Tìm kiếm sản phẩm
- `GET /api/cart/{userId}` - Lấy giỏ hàng
- `POST /api/cart/add` - Thêm vào giỏ hàng
- `GET /api/orders/user/{userId}` - Lịch sử đơn hàng
- `POST /api/orders/create` - Tạo đơn hàng
- `GET /api/vouchers/available` - Danh sách voucher khả dụng

### Admin APIs

- `GET /api/admin/products` - Danh sách sản phẩm
- `POST /api/admin/products` - Tạo sản phẩm
- `PUT /api/admin/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/admin/products/{id}` - Xóa sản phẩm
- `GET /api/admin/orders/search` - Tìm kiếm đơn hàng
- `PUT /api/admin/orders/{id}/status` - Cập nhật trạng thái đơn hàng
- Tương tự cho Brand, Category, Color, Voucher

## Database Schema

Các bảng chính:

- `users` - Người dùng
- `products` - Sản phẩm
- `brands` - Thương hiệu
- `categories` - Danh mục
- `colors` - Màu sắc
- `product_colors` - Liên kết sản phẩm-màu sắc
- `vouchers` - Mã giảm giá
- `addresses` - Địa chỉ
- `carts` - Giỏ hàng
- `orders` - Đơn hàng
- `order_items` - Chi tiết đơn hàng
- `reviews` - Đánh giá

## Ghi chú

- Mật khẩu được mã hóa bằng BCrypt
- JWT hoặc Session-based authentication có thể được thêm vào
- Email service cần được cấu hình với SMTP server thực tế
- File upload cho hình ảnh cần được cấu hình

## Phát triển tiếp

- [ ] Tích hợp thanh toán trực tuyến (VNPay, MoMo)
- [ ] Email service thực tế
- [ ] Upload hình ảnh lên cloud storage
- [ ] Redis cho cache
- [ ] Elasticsearch cho tìm kiếm nâng cao
- [ ] WebSocket cho thông báo real-time

## License

Copyright © 2024 VinFast Motorbike Store

