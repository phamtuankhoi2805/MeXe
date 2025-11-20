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