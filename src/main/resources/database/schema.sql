-- Create Database ASM_Java6
IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = 'ASM_Java6')
BEGIN
    CREATE DATABASE ASM_Java6;
END
GO

USE ASM_Java6;
GO

-- =============================================
-- 1. DROP TABLES IF EXISTS (Clean Slate)
-- =============================================
-- Uncomment if you want to drop tables before creating
/*
DROP TABLE IF EXISTS [dbo].[reviews];
DROP TABLE IF EXISTS [dbo].[order_items];
DROP TABLE IF EXISTS [dbo].[orders];
DROP TABLE IF EXISTS [dbo].[carts];
DROP TABLE IF EXISTS [dbo].[addresses];
DROP TABLE IF EXISTS [dbo].[vouchers];
DROP TABLE IF EXISTS [dbo].[product_colors];
DROP TABLE IF EXISTS [dbo].[product_images];
DROP TABLE IF EXISTS [dbo].[products];
DROP TABLE IF EXISTS [dbo].[colors];
DROP TABLE IF EXISTS [dbo].[categories];
DROP TABLE IF EXISTS [dbo].[brands];
DROP TABLE IF EXISTS [dbo].[users];
*/

-- =============================================
-- 2. CREATE TABLES
-- =============================================

-- Create Users Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Brands Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[brands]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[brands] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NOT NULL UNIQUE,
    [logo] NVARCHAR(500) NULL,
    [description] NVARCHAR(1000) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE()
);
END
GO

-- Create Categories Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[categories]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[categories] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NOT NULL UNIQUE,
    [description] NVARCHAR(1000) NULL,
    [image] NVARCHAR(500) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE()
);
END
GO

-- Create Colors Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[colors]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[colors] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(100) NOT NULL UNIQUE,
    [hex_code] NVARCHAR(7) NULL UNIQUE, -- Thêm UNIQUE cho hex_code
    [image] NVARCHAR(500) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE()
);
END
GO

-- Create Products Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[products]') AND type in (N'U'))
BEGIN
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
    [image] NVARCHAR(500) NULL, -- Ảnh đại diện chính
    -- [images] NVARCHAR(MAX) NULL, -- Deprecated: Đã chuyển sang bảng product_images
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
END
GO

-- Create Product_Images Table (New)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[product_images]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[product_images] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [product_id] BIGINT NOT NULL,
    [image_url] NVARCHAR(500) NOT NULL,
    [display_order] INT DEFAULT 0,
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[products]([id]) ON DELETE CASCADE
);
END
GO

-- Create Product_Colors Table (Many-to-Many)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[product_colors]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[product_colors] (
    [product_id] BIGINT NOT NULL,
    [color_id] BIGINT NOT NULL,
    [quantity] INT NOT NULL DEFAULT 0,
    PRIMARY KEY ([product_id], [color_id]),
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[products]([id]) ON DELETE CASCADE,
    FOREIGN KEY ([color_id]) REFERENCES [dbo].[colors]([id]) ON DELETE CASCADE
);
END
GO

-- Create Vouchers Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[vouchers]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Addresses Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[addresses]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Carts Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[carts]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Orders Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[orders]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Order_Items Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[order_items]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Reviews Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[reviews]') AND type in (N'U'))
BEGIN
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
END
GO

-- Create Indexes for better performance
-- Use IF NOT EXISTS for indexes as well
IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_users_email' AND object_id = OBJECT_ID('dbo.users'))
    CREATE INDEX [IX_users_email] ON [dbo].[users]([email]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_users_provider' AND object_id = OBJECT_ID('dbo.users'))
    CREATE INDEX [IX_users_provider] ON [dbo].[users]([provider], [provider_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_products_brand' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX [IX_products_brand] ON [dbo].[products]([brand_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_products_category' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX [IX_products_category] ON [dbo].[products]([category_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_products_slug' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX [IX_products_slug] ON [dbo].[products]([slug]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_products_status' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX [IX_products_status] ON [dbo].[products]([status]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_orders_user' AND object_id = OBJECT_ID('dbo.orders'))
    CREATE INDEX [IX_orders_user] ON [dbo].[orders]([user_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_orders_status' AND object_id = OBJECT_ID('dbo.orders'))
    CREATE INDEX [IX_orders_status] ON [dbo].[orders]([order_status]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_orders_order_code' AND object_id = OBJECT_ID('dbo.orders'))
    CREATE INDEX [IX_orders_order_code] ON [dbo].[orders]([order_code]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_carts_user' AND object_id = OBJECT_ID('dbo.carts'))
    CREATE INDEX [IX_carts_user] ON [dbo].[carts]([user_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_addresses_user' AND object_id = OBJECT_ID('dbo.addresses'))
    CREATE INDEX [IX_addresses_user] ON [dbo].[addresses]([user_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_reviews_product' AND object_id = OBJECT_ID('dbo.reviews'))
    CREATE INDEX [IX_reviews_product] ON [dbo].[reviews]([product_id]);

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'IX_reviews_user' AND object_id = OBJECT_ID('dbo.reviews'))
    CREATE INDEX [IX_reviews_user] ON [dbo].[reviews]([user_id]);
GO

-- =============================================
-- 3. SEED DATA (DATA.SQL CONTENT)
-- =============================================

-- 3.1 Insert Default Admin User
IF NOT EXISTS (SELECT * FROM [dbo].[users] WHERE email = 'admin@vinfast.com')
BEGIN
    INSERT INTO [dbo].[users] ([email], [password], [full_name], [phone], [role], [enabled], [email_verified])
    VALUES ('admin@vinfast.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt5eKl/K', N'Administrator', '0123456789', 'ADMIN', 1, 1);
END
GO

-- 3.2 Insert Categories
IF NOT EXISTS (SELECT * FROM [dbo].[categories] WHERE name = N'Xe máy điện phổ thông')
BEGIN
    INSERT INTO [dbo].[categories] ([name], [description]) VALUES 
    (N'Xe máy điện phổ thông', N'Dòng xe phù hợp học sinh, sinh viên, nội trợ'),
    (N'Xe máy điện trung cấp', N'Dòng xe công nghệ, quãng đường xa'),
    (N'Xe máy điện cao cấp', N'Dòng xe hiệu suất cao, công nghệ thông minh');
END
GO

-- 3.3 Insert Brands
IF NOT EXISTS (SELECT * FROM [dbo].[brands] WHERE name = N'VinFast')
BEGIN
    INSERT INTO [dbo].[brands] ([name], [description]) VALUES 
    (N'VinFast', N'Thương hiệu xe điện hàng đầu Việt Nam');
END
GO

-- 3.4 Insert Colors
IF NOT EXISTS (SELECT * FROM [dbo].[colors] WHERE name = N'Trắng Ngọc Trai')
BEGIN
    INSERT INTO [dbo].[colors] ([name], [hex_code], [image]) VALUES 
    (N'Trắng Ngọc Trai', '#FFFFFF', '/image/25-1.jpg'),
    (N'Đen Nhám', '#000000', '/image/evo200.jpg'),
    (N'Xanh Oliu', '#4CAF50', '/image/27-1.jpg');
END
GO

-- 3.5 Insert Vouchers
IF NOT EXISTS (SELECT * FROM [dbo].[vouchers] WHERE code = 'WELCOME10')
BEGIN
    INSERT INTO [dbo].[vouchers] ([code], [description], [discount_type], [discount_value], [min_order_amount], [quantity], [start_date], [end_date]) VALUES
    (N'WELCOME10', N'Giảm 10% cho khách hàng mới', 'PERCENTAGE', 10, 5000000, 1000, GETDATE(), DATEADD(month, 3, GETDATE())),
    (N'FREE50K', N'Giảm 50.000đ cho đơn hàng từ 10 triệu', 'FIXED', 50000, 10000000, 500, GETDATE(), DATEADD(month, 6, GETDATE()));
END
GO

-- 3.6 Insert Products (10 samples)
-- Only insert if table is empty or specific product not exists
IF NOT EXISTS (SELECT * FROM [dbo].[products] WHERE slug = 'vinfast-evo200')
BEGIN
    -- Clear old data if needed (be careful in production)
    DELETE FROM [dbo].[products];
    DBCC CHECKIDENT ('[dbo].[products]', RESEED, 0);

    INSERT INTO [dbo].[products] 
    ([name], [slug], [description], [price], [discount_price], [quantity], [brand_id], [category_id], [image], [specifications], [status]) 
    VALUES 
    -- 1. Evo200
    (N'VinFast Evo200', 'vinfast-evo200', N'Mẫu xe máy điện quốc dân với thiết kế thời trang, nhỏ gọn, phù hợp di chuyển trong đô thị. Quãng đường di chuyển lên tới 203km/lần sạc.', 22000000, 18000000, 50, 1, 1, '/image/evo200.jpg', N'Động cơ: 1500W; Pin: LFP; Tốc độ tối đa: 70km/h', 'ACTIVE'),

    -- 2. Evo200 Lite
    (N'VinFast Evo200 Lite', 'vinfast-evo200-lite', N'Phiên bản giới hạn tốc độ dành cho học sinh, không cần bằng lái. Vận hành êm ái, an toàn tuyệt đối.', 22000000, NULL, 100, 1, 1, '/image/25-1.jpg', N'Động cơ: 1500W; Pin: LFP; Tốc độ tối đa: 49km/h', 'ACTIVE'),

    -- 3. Feliz S
    (N'VinFast Feliz S', 'vinfast-feliz-s', N'Thiết kế thanh lịch, cốp rộng 25L, phù hợp cho phái đẹp. Công nghệ pin LFP tiên tiến giúp tăng tuổi thọ và quãng đường di chuyển.', 29900000, 27000000, 30, 1, 2, '/image/27-1.jpg', N'Động cơ: 1800W; Pin: LFP; Tốc độ tối đa: 78km/h', 'ACTIVE'),

    -- 4. Klara S (2022)
    (N'VinFast Klara S (2022)', 'vinfast-klara-s-2022', N'Biểu tượng của sự sang trọng và đẳng cấp. Thiết kế Ý tinh tế, công nghệ thông minh kết nối eSim.', 36900000, NULL, 20, 1, 2, '/image/25-1.jpg', N'Động cơ: 1800W; Pin: LFP; Tốc độ tối đa: 78km/h', 'ACTIVE'),

    -- 5. Vento S
    (N'VinFast Vento S', 'vinfast-vento-s', N'Mạnh mẽ và bứt phá. Sử dụng động cơ đặt bên (Side Motor) cho khả năng tăng tốc vượt trội.', 56000000, 50000000, 15, 1, 3, '/image/evo200.jpg', N'Động cơ: 3000W; Pin: LFP; Tốc độ tối đa: 89km/h', 'ACTIVE'),

    -- 6. Theon S
    (N'VinFast Theon S', 'vinfast-theon-s', N'Đỉnh cao công nghệ xe máy điện. Tốc độ tối đa 99km/h, phanh ABS 2 kênh an toàn tuyệt đối.', 69900000, NULL, 10, 1, 3, '/image/27-1.jpg', N'Động cơ: 3500W; Pin: LFP; Tốc độ tối đa: 99km/h', 'ACTIVE'),

    -- 7. Impes
    (N'VinFast Impes', 'vinfast-impes', N'Thiết kế thể thao, cá tính dành cho giới trẻ năng động. Gầm cao, vượt địa hình tốt.', 14900000, NULL, 0, 1, 1, '/image/evo200.jpg', N'Động cơ: 1200W; Pin: Lithium-ion; Tốc độ tối đa: 49km/h', 'OUT_OF_STOCK'),

    -- 8. Ludo
    (N'VinFast Ludo', 'vinfast-ludo', N'Nhỏ gọn, linh hoạt, giá thành hợp lý. Bạn đồng hành tin cậy trên mọi nẻo đường.', 12900000, NULL, 5, 1, 1, '/image/25-1.jpg', N'Động cơ: 500W; Pin: Lithium-ion; Tốc độ tối đa: 35km/h', 'ACTIVE'),

    -- 9. Tempest
    (N'VinFast Tempest', 'vinfast-tempest', N'Mẫu xe ý tưởng với thiết kế hầm hố, phá cách. Đang trong giai đoạn thử nghiệm thị trường.', 19000000, NULL, 50, 1, 1, '/image/27-1.jpg', N'Động cơ: 1600W; Pin: LFP; Tốc độ tối đa: 49km/h', 'ACTIVE'),

    -- 10. Klara A2
    (N'VinFast Klara A2', 'vinfast-klara-a2', N'Phiên bản sử dụng ắc quy chì, tiết kiệm chi phí nhưng vẫn giữ nguyên thiết kế sang trọng của dòng Klara.', 25000000, 22000000, 40, 1, 2, '/image/25-1.jpg', N'Động cơ: 1200W; Ắc quy: Chì; Tốc độ tối đa: 60km/h', 'ACTIVE');
END
GO

-- 3.7 Insert Product Images
-- Only if table empty to avoid duplicates
IF NOT EXISTS (SELECT * FROM [dbo].[product_images])
BEGIN
    INSERT INTO [dbo].[product_images] ([product_id], [image_url], [display_order])
    SELECT id, '/image/25-1.jpg', 1 FROM [dbo].[products]
    UNION ALL
    SELECT id, '/image/evo200.jpg', 2 FROM [dbo].[products]
    UNION ALL
    SELECT id, '/image/27-1.jpg', 3 FROM [dbo].[products];
END
GO

-- 3.8 Insert Product Colors
IF NOT EXISTS (SELECT * FROM [dbo].[product_colors])
BEGIN
    INSERT INTO [dbo].[product_colors] ([product_id], [color_id], [quantity])
    SELECT p.id, c.id, 20 -- Mỗi màu có 20 sản phẩm
    FROM [dbo].[products] p
    CROSS JOIN [dbo].[colors] c;
END
GO

-- =============================================
-- 4. CREATE CHARGING STATIONS TABLE
-- =============================================

-- Create Charging Stations Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[charging_stations]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[charging_stations] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NOT NULL,
    [address] NVARCHAR(500) NOT NULL,
    [latitude] DECIMAL(10,8) NOT NULL,
    [longitude] DECIMAL(11,8) NOT NULL,
    [available_batteries] INT NOT NULL DEFAULT 0,
    [total_capacity] INT NOT NULL DEFAULT 0,
    [phone] NVARCHAR(20) NULL,
    [operating_hours] NVARCHAR(100) NULL DEFAULT '24/7',
    [status] NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    [province] NVARCHAR(100) NULL,
    [district] NVARCHAR(100) NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [CK_charging_stations_status] CHECK ([status] IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    CONSTRAINT [CK_charging_stations_available] CHECK ([available_batteries] >= 0 AND [available_batteries] <= [total_capacity])
);
END
GO

-- Insert Sample Charging Stations Data
IF NOT EXISTS (SELECT * FROM [dbo].[charging_stations])
BEGIN
    INSERT INTO [dbo].[charging_stations] 
    ([name], [address], [latitude], [longitude], [available_batteries], [total_capacity], [phone], [operating_hours], [status], [province], [district], [created_at], [updated_at])
    VALUES
    -- Hà Nội
    (N'Trạm sạc VinFast Cầu Giấy', N'123 Đường Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy', 21.0285, 105.8542, 15, 20, '0241234567', '24/7', 'ACTIVE', N'Hà Nội', N'Cầu Giấy', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Hoàn Kiếm', N'456 Phố Hàng Bông, Phường Hàng Gai, Quận Hoàn Kiếm', 21.0285, 105.8542, 12, 18, '0241234568', '24/7', 'ACTIVE', N'Hà Nội', N'Hoàn Kiếm', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Đống Đa', N'789 Đường Láng, Phường Láng Thượng, Quận Đống Đa', 21.0142, 105.8022, 18, 25, '0241234569', '24/7', 'ACTIVE', N'Hà Nội', N'Đống Đa', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Hai Bà Trưng', N'321 Phố Bạch Mai, Phường Bạch Mai, Quận Hai Bà Trưng', 21.0056, 105.8542, 10, 15, '0241234570', '24/7', 'ACTIVE', N'Hà Nội', N'Hai Bà Trưng', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Ba Đình', N'654 Đường Điện Biên Phủ, Phường Điện Biên, Quận Ba Đình', 21.0333, 105.8400, 20, 30, '0241234571', '24/7', 'ACTIVE', N'Hà Nội', N'Ba Đình', GETDATE(), GETDATE()),
    
    -- TP. Hồ Chí Minh
    (N'Trạm sạc VinFast Quận 1', N'123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1', 10.7769, 106.7009, 25, 35, '0281234567', '24/7', 'ACTIVE', N'TP. Hồ Chí Minh', N'Quận 1', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Quận 3', N'456 Đường Lê Văn Sỹ, Phường 12, Quận 3', 10.7831, 106.6967, 18, 28, '0281234568', '24/7', 'ACTIVE', N'TP. Hồ Chí Minh', N'Quận 3', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Quận 7', N'789 Đường Nguyễn Thị Thập, Phường Tân Phú, Quận 7', 10.7314, 106.7214, 22, 32, '0281234569', '24/7', 'ACTIVE', N'TP. Hồ Chí Minh', N'Quận 7', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Bình Thạnh', N'321 Đường Xô Viết Nghệ Tĩnh, Phường 25, Quận Bình Thạnh', 10.8022, 106.7147, 15, 25, '0281234570', '24/7', 'ACTIVE', N'TP. Hồ Chí Minh', N'Bình Thạnh', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Tân Bình', N'654 Đường Cộng Hòa, Phường 13, Quận Tân Bình', 10.8014, 106.6528, 20, 30, '0281234571', '24/7', 'ACTIVE', N'TP. Hồ Chí Minh', N'Tân Bình', GETDATE(), GETDATE()),
    
    -- Đà Nẵng
    (N'Trạm sạc VinFast Hải Châu', N'123 Đường Bạch Đằng, Phường Hải Châu 1, Quận Hải Châu', 16.0544, 108.2022, 12, 20, '0236123456', '24/7', 'ACTIVE', N'Đà Nẵng', N'Hải Châu', GETDATE(), GETDATE()),
    (N'Trạm sạc VinFast Thanh Khê', N'456 Đường Lê Duẩn, Phường Thanh Khê Tây, Quận Thanh Khê', 16.0683, 108.1917, 10, 18, '0236123457', '24/7', 'ACTIVE', N'Đà Nẵng', N'Thanh Khê', GETDATE(), GETDATE()),
    
    -- Cần Thơ
    (N'Trạm sạc VinFast Ninh Kiều', N'123 Đường Nguyễn Thái Học, Phường Cái Khế, Quận Ninh Kiều', 10.0452, 105.7469, 8, 15, '0292123456', '24/7', 'ACTIVE', N'Cần Thơ', N'Ninh Kiều', GETDATE(), GETDATE()),
    
    -- Hải Phòng
    (N'Trạm sạc VinFast Hồng Bàng', N'456 Đường Lạch Tray, Phường Đằng Giang, Quận Ngô Quyền', 20.8449, 106.6881, 14, 22, '0225123456', '24/7', 'ACTIVE', N'Hải Phòng', N'Ngô Quyền', GETDATE(), GETDATE());
END
GO