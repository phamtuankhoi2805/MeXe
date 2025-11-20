-- Create Database VinFastMotorbikeDB


-- Create Users Table
CREATE TABLE [dbo].[users] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [email] NVARCHAR(255) NOT NULL UNIQUE,
    [password] NVARCHAR(255) NULL, -- Cho phép NULL nếu dùng OAuth/provider
    [full_name] NVARCHAR(255) NOT NULL,
    [phone] NVARCHAR(20) NULL,
    [avatar] NVARCHAR(500) NULL,
    [role] NVARCHAR(20) NOT NULL DEFAULT 'USER',
    [provider] NVARCHAR(20) NULL,
    [provider_id] NVARCHAR(255) NULL,
    [enabled] BIT NOT NULL DEFAULT 1,
    [email_verified] BIT NOT NULL DEFAULT 0,
    [verification_token] NVARCHAR(255) NULL,
    [reset_token] NVARCHAR(255) NULL,
    [reset_token_expiry] DATETIME NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    -- Ràng buộc: phải có password hoặc provider/provider_id
    CONSTRAINT [CK_users_auth_method] CHECK ([password] IS NOT NULL OR ([provider] IS NOT NULL AND [provider_id] IS NOT NULL)),
    CONSTRAINT [CK_users_role] CHECK ([role] IN ('USER', 'ADMIN'))
);

-- Create Brands Table
CREATE TABLE [dbo].[brands] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NOT NULL UNIQUE,
    [logo] NVARCHAR(500) NULL,
    [description] NVARCHAR(1000) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE()
);

-- Create Categories Table
CREATE TABLE [dbo].[categories] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NOT NULL UNIQUE,
    [description] NVARCHAR(1000) NULL,
    [image] NVARCHAR(500) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE()
);

-- Create Colors Table
CREATE TABLE [dbo].[colors] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(100) NOT NULL UNIQUE,
    [hex_code] NVARCHAR(7) NULL UNIQUE, -- Thêm UNIQUE cho hex_code
    [image] NVARCHAR(500) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE()
);

-- Create Products Table
CREATE TABLE [dbo].[products] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NOT NULL,
    [slug] NVARCHAR(255) NOT NULL UNIQUE,
    [description] NVARCHAR(MAX) NULL,
    [price] DECIMAL(18,2) NOT NULL,
    [discount_price] DECIMAL(18,2) NULL,
    [quantity] INT NOT NULL DEFAULT 0,
    [brand_id] BIGINT NULL,
    [category_id] BIGINT NULL,
    [image] NVARCHAR(500) NULL,
    [images] NVARCHAR(MAX) NULL,
    [specifications] NVARCHAR(MAX) NULL,
    [status] NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    -- Đảm bảo discount_price <= price
    CONSTRAINT [CK_products_discount] CHECK ([discount_price] IS NULL OR [discount_price] <= [price]), 
    FOREIGN KEY ([brand_id]) REFERENCES [dbo].[brands]([id]) ON DELETE SET NULL,
    FOREIGN KEY ([category_id]) REFERENCES [dbo].[categories]([id]) ON DELETE SET NULL,
    CONSTRAINT [CK_products_status] CHECK ([status] IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK'))
);

-- Create Product_Colors Table (Many-to-Many)
CREATE TABLE [dbo].[product_colors] (
    [product_id] BIGINT NOT NULL,
    [color_id] BIGINT NOT NULL,
    [quantity] INT NOT NULL DEFAULT 0,
    PRIMARY KEY ([product_id], [color_id]),
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[products]([id]) ON DELETE CASCADE,
    FOREIGN KEY ([color_id]) REFERENCES [dbo].[colors]([id]) ON DELETE CASCADE
);

-- Create Vouchers Table
CREATE TABLE [dbo].[vouchers] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [code] NVARCHAR(50) NOT NULL UNIQUE,
    [description] NVARCHAR(500) NULL,
    [discount_type] NVARCHAR(20) NOT NULL,
    [discount_value] DECIMAL(18,2) NOT NULL,
    [min_order_amount] DECIMAL(18,2) NULL,
    [max_discount_amount] DECIMAL(18,2) NULL,
    [quantity] INT NOT NULL DEFAULT 0,
    [used_count] INT NOT NULL DEFAULT 0,
    [start_date] DATETIME NOT NULL,
    [end_date] DATETIME NOT NULL,
    [status] NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    -- Đảm bảo ngày bắt đầu <= ngày kết thúc
    CONSTRAINT [CK_vouchers_date] CHECK ([start_date] <= [end_date]), 
    CONSTRAINT [CK_vouchers_discount_type] CHECK ([discount_type] IN ('PERCENTAGE', 'FIXED')),
    CONSTRAINT [CK_vouchers_status] CHECK ([status] IN ('ACTIVE', 'INACTIVE', 'EXPIRED'))
);

-- Create Addresses Table
CREATE TABLE [dbo].[addresses] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [user_id] BIGINT NOT NULL,
    [full_name] NVARCHAR(255) NOT NULL,
    [phone] NVARCHAR(20) NOT NULL,
    [province] NVARCHAR(100) NOT NULL,
    [district] NVARCHAR(100) NOT NULL,
    [ward] NVARCHAR(100) NOT NULL,
    [street] NVARCHAR(500) NOT NULL,
    [is_default] BIT NOT NULL DEFAULT 0,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([id]) ON DELETE CASCADE
);

-- Create Carts Table
CREATE TABLE [dbo].[carts] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [user_id] BIGINT NOT NULL,
    [product_id] BIGINT NOT NULL,
    [color_id] BIGINT NULL,
    [quantity] INT NOT NULL DEFAULT 1,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([id]) ON DELETE CASCADE,
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[products]([id]) ON DELETE CASCADE,
    FOREIGN KEY ([color_id]) REFERENCES [dbo].[colors]([id]) ON DELETE SET NULL,
    -- Giữ ràng buộc UNIQUE ([user_id], [product_id], [color_id]) để đảm bảo mỗi item/color chỉ có 1 dòng trong giỏ hàng
    UNIQUE ([user_id], [product_id], [color_id]) 
);

-- Create Orders Table
CREATE TABLE [dbo].[orders] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [order_code] NVARCHAR(50) NOT NULL UNIQUE,
    [user_id] BIGINT NOT NULL,
    [address_id] BIGINT NOT NULL,
    [voucher_id] BIGINT NULL,
    [subtotal] DECIMAL(18,2) NOT NULL,
    [discount] DECIMAL(18,2) NOT NULL DEFAULT 0,
    [shipping_fee] DECIMAL(18,2) NOT NULL DEFAULT 0,
    [total] DECIMAL(18,2) NOT NULL,
    [payment_method] NVARCHAR(50) NOT NULL,
    [payment_status] NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    [order_status] NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    [delivery_method] NVARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    [fast_delivery_status] NVARCHAR(50) NULL,
    [tracking_number] NVARCHAR(100) NULL,
    [notes] NVARCHAR(1000) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    -- Đảm bảo Total là tổng của subtotal + shipping_fee - discount
    CONSTRAINT [CK_orders_total] CHECK ([total] = [subtotal] + [shipping_fee] - [discount]), 
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([id]) ON DELETE NO ACTION, -- Đơn hàng nên tồn tại ngay cả khi user bị xóa (chuyển sang trạng thái ẩn danh)
    FOREIGN KEY ([address_id]) REFERENCES [dbo].[addresses]([id]) ON DELETE NO ACTION, -- Địa chỉ không nên bị xóa nếu đang được order tham chiếu
    FOREIGN KEY ([voucher_id]) REFERENCES [dbo].[vouchers]([id]) ON DELETE SET NULL,
    CONSTRAINT [CK_orders_payment_status] CHECK ([payment_status] IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    CONSTRAINT [CK_orders_order_status] CHECK ([order_status] IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'RETURNED')),
    CONSTRAINT [CK_orders_delivery_method] CHECK ([delivery_method] IN ('STANDARD', 'FAST'))
);

-- Create Order_Items Table
CREATE TABLE [dbo].[order_items] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [order_id] BIGINT NOT NULL,
    [product_id] BIGINT NOT NULL,
    [color_id] BIGINT NULL,
    [product_name] NVARCHAR(255) NOT NULL,
    [product_image] NVARCHAR(500) NULL,
    [color_name] NVARCHAR(100) NULL,
    [price] DECIMAL(18,2) NOT NULL,
    [quantity] INT NOT NULL,
    [subtotal] DECIMAL(18,2) NOT NULL,
    FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders]([id]) ON DELETE CASCADE,
    -- Sửa: Xóa ON DELETE CASCADE/SET NULL để bảo toàn thông tin chi tiết đơn hàng
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[products]([id]) ON DELETE NO ACTION, 
    FOREIGN KEY ([color_id]) REFERENCES [dbo].[colors]([id]) ON DELETE NO ACTION 
);

-- Create Reviews Table
CREATE TABLE [dbo].[reviews] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [user_id] BIGINT NOT NULL,
    [order_id] BIGINT NOT NULL,
    [product_id] BIGINT NOT NULL,
    [rating] INT NOT NULL,
    [comment] NVARCHAR(1000) NULL,
    [images] NVARCHAR(MAX) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    -- Ràng buộc UNIQUE để 1 đơn hàng chỉ có thể đánh giá 1 sản phẩm 1 lần (nếu mỗi item trong order là 1 sản phẩm)
    UNIQUE ([user_id], [order_id], [product_id]), 
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([id]) ON DELETE CASCADE,
    FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders]([id]) ON DELETE CASCADE,
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[products]([id]) ON DELETE CASCADE,
    CONSTRAINT [CK_reviews_rating] CHECK ([rating] >= 1 AND [rating] <= 5)
);

-- Create Indexes for better performance
CREATE INDEX [IX_users_email] ON [dbo].[users]([email]);
CREATE INDEX [IX_users_provider] ON [dbo].[users]([provider], [provider_id]);
CREATE INDEX [IX_products_brand] ON [dbo].[products]([brand_id]);
CREATE INDEX [IX_products_category] ON [dbo].[products]([category_id]);
CREATE INDEX [IX_products_slug] ON [dbo].[products]([slug]);
CREATE INDEX [IX_products_status] ON [dbo].[products]([status]);
CREATE INDEX [IX_orders_user] ON [dbo].[orders]([user_id]);
CREATE INDEX [IX_orders_status] ON [dbo].[orders]([order_status]);
CREATE INDEX [IX_orders_order_code] ON [dbo].[orders]([order_code]);
CREATE INDEX [IX_carts_user] ON [dbo].[carts]([user_id]);
CREATE INDEX [IX_addresses_user] ON [dbo].[addresses]([user_id]);
CREATE INDEX [IX_reviews_product] ON [dbo].[reviews]([product_id]);
CREATE INDEX [IX_reviews_user] ON [dbo].[reviews]([user_id]);

-- Insert Default Admin User (password: admin123 - should be hashed in BCrypt)
INSERT INTO [dbo].[users] ([email], [password], [full_name], [phone], [role], [enabled], [email_verified])
VALUES ('admin@vinfast.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt5eKl/K', N'Administrator', '0123456789', 'ADMIN', 1, 1);

-- Insert Sample Brands
INSERT INTO [dbo].[brands] ([name], [description]) VALUES
(N'VinFast', N'Thương hiệu xe máy điện hàng đầu Việt Nam'),
(N'Pega', N'Xe máy điện hiện đại và tiện lợi'),
(N'Ebike', N'Xe đạp điện và xe máy điện chất lượng cao');

-- Insert Sample Categories
INSERT INTO [dbo].[categories] ([name], [description]) VALUES
(N'Xe máy điện', N'Xe máy điện phục vụ di chuyển hàng ngày'),
(N'Xe đạp điện', N'Xe đạp điện thân thiện môi trường'),
(N'Phụ kiện', N'Phụ kiện và đồ chơi cho xe máy điện');

-- Insert Sample Colors
INSERT INTO [dbo].[colors] ([name], [hex_code]) VALUES
(N'Đen', '#000000'),
(N'Trắng', '#FFFFFF'),
(N'Đỏ', '#FF0000'),
(N'Xanh dương', '#0066FF'),
(N'Xanh lá', '#00CC00'),
(N'Vàng', '#FFCC00'),
(N'Xám', '#808080');

-- Insert Sample Vouchers
INSERT INTO [dbo].[vouchers] ([code], [description], [discount_type], [discount_value], [min_order_amount], [quantity], [start_date], [end_date]) VALUES
(N'WELCOME10', N'Giảm 10% cho khách hàng mới', 'PERCENTAGE', 10, 5000000, 1000, GETDATE(), DATEADD(month, 3, GETDATE())),
(N'FREE50K', N'Giảm 50.000đ cho đơn hàng từ 10 triệu', 'FIXED', 50000, 10000000, 500, GETDATE(), DATEADD(month, 6, GETDATE()));

GO
-- =============================================
-- BẮT ĐẦU DỮ LIỆU MẪU (SEED DATA) - FIXED
-- =============================================

-- 1. THÊM NHÃN HIỆU (BRANDS) MỚI
INSERT INTO [dbo].[brands] ([name], [description]) VALUES
(N'Dat Bike', N'Xe máy điện startup Việt Nam, hiệu năng cao'),
(N'Yadea', N'Thương hiệu xe máy điện quốc tế bán chạy');

-- 2. THÊM DANH MỤC (CATEGORIES) MỚI
INSERT INTO [dbo].[categories] ([name], [description]) VALUES
(N'Pin & Sạc', N'Pin LFP và bộ sạc chính hãng');

-- 3. LẤY ID ĐỂ DÙNG CHO CÁC BẢNG SAU (Biến tạm)
DECLARE @BrandVinFast BIGINT = (SELECT id FROM brands WHERE name = N'VinFast');
DECLARE @BrandPega BIGINT = (SELECT id FROM brands WHERE name = N'Pega');
DECLARE @BrandDatBike BIGINT = (SELECT id FROM brands WHERE name = N'Dat Bike');
DECLARE @BrandYadea BIGINT = (SELECT id FROM brands WHERE name = N'Yadea');

DECLARE @CatMoto BIGINT = (SELECT id FROM categories WHERE name = N'Xe máy điện');
DECLARE @CatAcc BIGINT = (SELECT id FROM categories WHERE name = N'Phụ kiện');
DECLARE @CatBat BIGINT = (SELECT id FROM categories WHERE name = N'Pin & Sạc');

DECLARE @ColorBlack BIGINT = (SELECT id FROM colors WHERE hex_code = '#000000');
DECLARE @ColorWhite BIGINT = (SELECT id FROM colors WHERE hex_code = '#FFFFFF');
DECLARE @ColorRed BIGINT = (SELECT id FROM colors WHERE hex_code = '#FF0000');
DECLARE @ColorBlue BIGINT = (SELECT id FROM colors WHERE hex_code = '#0066FF');
DECLARE @ColorGreen BIGINT = (SELECT id FROM colors WHERE hex_code = '#00CC00');

-- 4. THÊM SẢN PHẨM (PRODUCTS)
INSERT INTO [dbo].[products] ([name], [slug], [description], [price], [discount_price], [quantity], [brand_id], [category_id], [image], [specifications], [status]) VALUES
-- VinFast Bikes
(N'VinFast Feliz S', 'vinfast-feliz-s', N'Mẫu xe máy điện quốc dân, pin LFP đi được 198km/lần sạc.', 27000000, 26500000, 50, @BrandVinFast, @CatMoto, N'https://placehold.co/500x500?text=Feliz+S', N'{"range":"198km", "max_speed":"78km/h", "battery":"LFP 3.5kWh"}', 'ACTIVE'),
(N'VinFast Klara S (2022)', 'vinfast-klara-s-2022', N'Thiết kế Ý thanh lịch, vận hành êm ái, cốp rộng.', 35000000, NULL, 30, @BrandVinFast, @CatMoto, N'https://placehold.co/500x500?text=Klara+S', N'{"range":"194km", "max_speed":"78km/h", "trunk":"23L"}', 'ACTIVE'),
(N'VinFast Evo200', 'vinfast-evo200', N'Xe máy điện thời trang, nhỏ gọn, di chuyển linh hoạt trong phố.', 22000000, 18000000, 100, @BrandVinFast, @CatMoto, N'https://placehold.co/500x500?text=Evo200', N'{"range":"203km", "max_speed":"70km/h"}', 'ACTIVE'),
(N'VinFast Vento S', 'vinfast-vento-s', N'Công nghệ hiện đại, động cơ IPM đặt bên (Side Motor).', 50000000, 48000000, 15, @BrandVinFast, @CatMoto, N'https://placehold.co/500x500?text=Vento+S', N'{"range":"160km", "max_speed":"89km/h", "tech":"ABS"}', 'ACTIVE'),
(N'VinFast Theon S', 'vinfast-theon-s', N'Đỉnh cao công nghệ, tốc độ vượt trội tương đương xe xăng 300cc.', 63000000, NULL, 5, @BrandVinFast, @CatMoto, N'https://placehold.co/500x500?text=Theon+S', N'{"range":"150km", "max_speed":"99km/h", "tech":"ABS 2 kenh"}', 'ACTIVE'),

-- Dat Bike & Yadea
(N'Dat Bike Weaver++', 'dat-bike-weaver-plus', N'Xe máy điện mạnh nhất Việt Nam, sạc siêu nhanh.', 65900000, NULL, 20, @BrandDatBike, @CatMoto, N'https://placehold.co/500x500?text=Weaver++', N'{"range":"200km", "max_speed":"90km/h", "charge":"20min-100km"}', 'ACTIVE'),
(N'Yadea Odora', 'yadea-odora', N'Thiết kế sành điệu, công nghệ TTFAR.', 18990000, 17500000, 40, @BrandYadea, @CatMoto, N'https://placehold.co/500x500?text=Odora', N'{"range":"100km", "max_speed":"50km/h"}', 'ACTIVE'),

-- Phụ kiện
(N'Mũ bảo hiểm 3/4 VinFast', 'mu-bao-hiem-3-4', N'Mũ bảo hiểm cao cấp, an toàn chuẩn DOT.', 850000, 600000, 200, @BrandVinFast, @CatAcc, N'https://placehold.co/500x500?text=Helmet', NULL, 'ACTIVE'),
(N'Bộ sạc di động 1.2kW', 'bo-sac-di-dong', N'Sạc cầm tay tiện lợi cho xe VinFast LFP.', 3500000, NULL, 50, @BrandVinFast, @CatBat, N'https://placehold.co/500x500?text=Charger', NULL, 'ACTIVE');

-- 5. THIẾT LẬP KHO MÀU SẮC (PRODUCT_COLORS)
DECLARE @P_FelizS BIGINT = (SELECT id FROM products WHERE slug = 'vinfast-feliz-s');
DECLARE @P_KlaraS BIGINT = (SELECT id FROM products WHERE slug = 'vinfast-klara-s-2022');
DECLARE @P_Weaver BIGINT = (SELECT id FROM products WHERE slug = 'dat-bike-weaver-plus');
DECLARE @P_Helmet BIGINT = (SELECT id FROM products WHERE slug = 'mu-bao-hiem-3-4');

INSERT INTO [dbo].[product_colors] ([product_id], [color_id], [quantity]) VALUES
(@P_FelizS, @ColorWhite, 20),
(@P_FelizS, @ColorBlack, 15),
(@P_FelizS, @ColorGreen, 15),
(@P_KlaraS, @ColorRed, 10),
(@P_KlaraS, @ColorBlue, 20),
(@P_Weaver, @ColorRed, 10),
(@P_Weaver, @ColorBlack, 10),
(@P_Helmet, @ColorBlack, 100),
(@P_Helmet, @ColorWhite, 100);

-- 6. THÊM NGƯỜI DÙNG (USERS)
DECLARE @DefPass NVARCHAR(255) = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt5eKl/K';

INSERT INTO [dbo].[users] ([email], [password], [full_name], [phone], [role], [enabled], [email_verified]) VALUES
('khachhang1@gmail.com', @DefPass, N'Nguyễn Văn An', '0901234567', 'USER', 1, 1),
('khachhang2@gmail.com', @DefPass, N'Trần Thị Bích', '0909876543', 'USER', 1, 1),
('khachhang3@gmail.com', @DefPass, N'Lê Hoàng Cường', '0912345678', 'USER', 1, 0),
('pro_rider@gmail.com', @DefPass, N'Phạm Minh Tuấn', '0988888888', 'USER', 1, 1);

-- 7. THÊM ĐỊA CHỈ (ADDRESSES)
DECLARE @U1 BIGINT = (SELECT id FROM users WHERE email = 'khachhang1@gmail.com');
DECLARE @U2 BIGINT = (SELECT id FROM users WHERE email = 'khachhang2@gmail.com');
DECLARE @U3 BIGINT = (SELECT id FROM users WHERE email = 'pro_rider@gmail.com');

INSERT INTO [dbo].[addresses] ([user_id], [full_name], [phone], [province], [district], [ward], [street], [is_default]) VALUES
(@U1, N'Nguyễn Văn An', '0901234567', N'Hà Nội', N'Quận Cầu Giấy', N'Phường Dịch Vọng', N'123 Xuân Thủy', 1),
(@U2, N'Trần Thị Bích', '0909876543', N'TP. Hồ Chí Minh', N'Quận 1', N'Phường Bến Nghé', N'45 Lê Thánh Tôn', 1),
(@U3, N'Phạm Minh Tuấn', '0988888888', N'Đà Nẵng', N'Quận Hải Châu', N'Phường Thạch Thang', N'88 Bạch Đằng', 1);

-- 8. TẠO ĐƠN HÀNG (ORDERS) & CHI TIẾT (ORDER_ITEMS)
-- Đơn hàng 1
DECLARE @Addr1 BIGINT = (SELECT TOP 1 id FROM addresses WHERE user_id = @U1);
INSERT INTO [dbo].[orders] ([order_code], [user_id], [address_id], [voucher_id], [subtotal], [discount], [shipping_fee], [total], [payment_method], [payment_status], [order_status], [created_at]) 
VALUES ('ORD-2025-001', @U1, @Addr1, NULL, 26500000, 0, 0, 26500000, 'COD', 'PAID', 'DELIVERED', DATEADD(day, -10, GETDATE()));

DECLARE @O1 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-2025-001');
INSERT INTO [dbo].[order_items] ([order_id], [product_id], [color_id], [product_name], [product_image], [color_name], [price], [quantity], [subtotal])
VALUES (@O1, @P_FelizS, @ColorWhite, N'VinFast Feliz S', N'https://placehold.co/500x500?text=Feliz+S', N'Trắng', 26500000, 1, 26500000);

-- Đơn hàng 2
DECLARE @Addr2 BIGINT = (SELECT TOP 1 id FROM addresses WHERE user_id = @U2);
INSERT INTO [dbo].[orders] ([order_code], [user_id], [address_id], [voucher_id], [subtotal], [discount], [shipping_fee], [total], [payment_method], [payment_status], [order_status], [created_at]) 
VALUES ('ORD-2025-002', @U2, @Addr2, NULL, 1200000, 0, 30000, 1230000, 'VNPAY', 'PAID', 'SHIPPING', DATEADD(day, -2, GETDATE()));

DECLARE @O2 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-2025-002');
INSERT INTO [dbo].[order_items] ([order_id], [product_id], [color_id], [product_name], [product_image], [color_name], [price], [quantity], [subtotal])
VALUES (@O2, @P_Helmet, @ColorBlack, N'Mũ bảo hiểm 3/4 VinFast', N'https://placehold.co/500x500?text=Helmet', N'Đen', 600000, 2, 1200000);

-- Đơn hàng 3
DECLARE @Addr3 BIGINT = (SELECT TOP 1 id FROM addresses WHERE user_id = @U3);
DECLARE @VoucherID BIGINT = (SELECT id FROM vouchers WHERE code = 'FREE50K');

INSERT INTO [dbo].[orders] ([order_code], [user_id], [address_id], [voucher_id], [subtotal], [discount], [shipping_fee], [total], [payment_method], [payment_status], [order_status], [created_at]) 
VALUES ('ORD-2025-003', @U3, @Addr3, @VoucherID, 65900000, 50000, 0, 65850000, 'BANK_TRANSFER', 'PENDING', 'PENDING', GETDATE());

DECLARE @O3 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-2025-003');
INSERT INTO [dbo].[order_items] ([order_id], [product_id], [color_id], [product_name], [product_image], [color_name], [price], [quantity], [subtotal])
VALUES (@O3, @P_Weaver, @ColorBlack, N'Dat Bike Weaver++', N'https://placehold.co/500x500?text=Weaver++', N'Đen', 65900000, 1, 65900000);

-- 9. THÊM ĐÁNH GIÁ (REVIEWS) - ĐÃ SỬA LỖI
INSERT INTO [dbo].[reviews] ([user_id], [order_id], [product_id], [rating], [comment], [created_at])
VALUES (@U1, @O1, @P_FelizS, 5, N'Xe đi rất êm, màu trắng sang trọng, giao hàng nhanh.', DATEADD(day, -5, GETDATE()));

-- Sửa lỗi: Thêm cột [created_at] vào danh sách cột
INSERT INTO [dbo].[reviews] ([user_id], [order_id], [product_id], [rating], [comment], [created_at])
VALUES (@U2, @O2, @P_Helmet, 4, N'Mũ đẹp nhưng hơi chật so với size mô tả.', GETDATE());

GO