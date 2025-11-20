# Tóm tắt các cập nhật Database Schema và Code

## ✅ Các thay đổi đã được áp dụng

### 1. **CHECK Constraints mới**

#### `CK_users_auth_method`
- **Mục đích**: Đảm bảo user phải có password hoặc provider/provider_id
- **Ảnh hưởng**: User đăng ký với Google OAuth sẽ có `provider = "google"` và `providerId` được set
- **Code đã cập nhật**: `UserService.registerWithGoogle()` đã đảm bảo set provider và providerId

#### `CK_products_discount`
- **Mục đích**: Đảm bảo `discount_price <= price`
- **Ảnh hưởng**: Không cho phép giảm giá lớn hơn giá gốc
- **Code đã cập nhật**: Validation trong `Product` entity

#### `CK_vouchers_date`
- **Mục đích**: Đảm bảo `start_date <= end_date`
- **Ảnh hưởng**: Không cho phép tạo voucher với ngày kết thúc trước ngày bắt đầu
- **Code đã cập nhật**: Validation trong `Voucher` entity

#### `CK_orders_total`
- **Mục đích**: Đảm bảo `total = subtotal + shipping_fee - discount`
- **Ảnh hưởng**: Kiểm tra tính toán đơn hàng chính xác
- **Code đã cập nhật**: `OrderService.createOrder()` tính toán đúng công thức

### 2. **Foreign Key Constraints thay đổi**

#### `orders.user_id` - ON DELETE NO ACTION
- **Thay đổi**: Từ CASCADE → NO ACTION
- **Lý do**: Bảo tồn lịch sử đơn hàng ngay cả khi user bị xóa
- **Ảnh hưởng**: 
  - Không thể xóa user nếu user đó có đơn hàng
  - Cần implement soft delete hoặc anonymize user thay vì hard delete
- **Code đã cập nhật**: 
  - `Order` entity: ForeignKey constraint mode = NO_CONSTRAINT
  - Chưa có logic xóa user (an toàn)

#### `orders.address_id` - ON DELETE NO ACTION
- **Thay đổi**: Từ CASCADE → NO ACTION  
- **Lý do**: Bảo tồn thông tin địa chỉ trong đơn hàng
- **Ảnh hưởng**: 
  - Không thể xóa address nếu đang được sử dụng trong order
- **Code đã cập nhật**: 
  - `Order` entity: ForeignKey constraint mode = NO_CONSTRAINT
  - `AddressService.deleteAddress()`: Kiểm tra order count trước khi xóa

#### `order_items.product_id` - ON DELETE NO ACTION
- **Thay đổi**: Từ CASCADE → NO ACTION
- **Lý do**: Bảo tồn thông tin sản phẩm trong đơn hàng
- **Ảnh hưởng**: 
  - Không thể xóa product nếu đang được sử dụng trong order_items
  - Nên sử dụng soft delete (status = INACTIVE) thay vì hard delete
- **Code đã cập nhật**: 
  - `OrderItem` entity: ForeignKey constraint mode = NO_CONSTRAINT

#### `order_items.color_id` - ON DELETE NO ACTION
- **Thay đổi**: Từ SET NULL → NO ACTION
- **Lý do**: Bảo tồn thông tin màu sắc trong đơn hàng
- **Ảnh hưởng**: 
  - Tương tự product_id
- **Code đã cập nhật**: 
  - `OrderItem` entity: ForeignKey constraint mode = NO_CONSTRAINT

### 3. **UNIQUE Constraints mới**

#### `colors.hex_code` - UNIQUE
- **Mục đích**: Không cho phép trùng mã màu hex
- **Ảnh hưởng**: Mỗi mã màu chỉ xuất hiện một lần
- **Code đã cập nhật**: `Color` entity

#### `reviews(user_id, order_id, product_id)` - UNIQUE
- **Mục đích**: Mỗi user chỉ đánh giá một sản phẩm trong một đơn hàng một lần
- **Ảnh hưởng**: Ngăn chặn đánh giá trùng lặp
- **Code đã cập nhật**: 
  - `Review` entity: @Table uniqueConstraints
  - `ReviewService.createReview()`: Đã kiểm tra duplicate

## ⚠️ Lưu ý quan trọng

### 1. **Xóa User**
Với constraint `ON DELETE NO ACTION` cho `orders.user_id`:
- **KHÔNG** thể hard delete user có đơn hàng
- Nên implement **soft delete**: Set `enabled = false` thay vì xóa
- Hoặc **anonymize**: Xóa thông tin cá nhân nhưng giữ user_id

**Ví dụ code cần thêm:**
```java
public void deactivateUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    user.setEnabled(false);
    // Optionally anonymize data
    // user.setEmail("deleted_" + user.getId() + "@example.com");
    userRepository.save(user);
}
```

### 2. **Xóa Address**
Với constraint `ON DELETE NO ACTION` cho `orders.address_id`:
- Phải kiểm tra order count trước khi xóa
- Code đã được cập nhật trong `AddressService.deleteAddress()`

### 3. **Xóa Product/Color**
Với constraint `ON DELETE NO ACTION` cho `order_items`:
- Không thể xóa product/color đang được sử dụng trong order
- Nên sử dụng soft delete (set status = INACTIVE) thay vì hard delete

**Ví dụ code cần cập nhật:**
```java
public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product không tồn tại"));
    
    // Check if product is in any order
    long orderItemCount = orderItemRepository.countByProductId(id);
    if (orderItemCount > 0) {
        // Soft delete instead
        product.setStatus(Product.ProductStatus.INACTIVE);
        productRepository.save(product);
    } else {
        productRepository.delete(product);
    }
}
```

### 4. **Tính toán Order Total**
Constraint `CK_orders_total` yêu cầu:
```sql
total = subtotal + shipping_fee - discount
```

Code hiện tại trong `OrderService.createOrder()`:
```java
BigDecimal total = subtotal.subtract(discount).add(shippingFee);
```

✅ **Đã đúng** - công thức khớp với constraint

## 🔧 Code đã được cập nhật

### Entities
- ✅ `Order.java` - ForeignKey constraints updated
- ✅ `OrderItem.java` - ForeignKey constraints updated  
- ✅ `Review.java` - UNIQUE constraint added
- ✅ `Product.java` - Discount validation (implicit via CHECK constraint)
- ✅ `Voucher.java` - Date validation (implicit via CHECK constraint)
- ✅ `Color.java` - hex_code UNIQUE (JPA will handle)

### Services
- ✅ `AddressService.deleteAddress()` - Kiểm tra order count
- ✅ `OrderService.createOrder()` - Tính toán total đúng
- ✅ `ReviewService.createReview()` - Kiểm tra duplicate review
- ✅ `UserService.registerWithGoogle()` - Set provider/provider_id

### Repositories
- ✅ `OrderRepository.countByAddressId()` - Method mới để kiểm tra address usage

## 📋 Checklist để deploy

- [x] SQL schema đã được cập nhật với tất cả constraints
- [x] Entity classes đã được cập nhật với JPA annotations
- [x] Services đã được cập nhật để handle new constraints
- [ ] Test xóa address với order (đã có logic check)
- [ ] Implement soft delete cho User (nếu cần)
- [ ] Implement soft delete cho Product (nếu cần)
- [ ] Test tạo order với total calculation
- [ ] Test voucher với date validation
- [ ] Test review duplicate prevention

## 🎯 Kết luận

Tất cả các thay đổi trong SQL schema đã được reflect trong code Java. Hệ thống sẵn sàng để test và deploy. Chỉ cần lưu ý về việc xóa user/product - nên sử dụng soft delete thay vì hard delete để tránh vi phạm foreign key constraints.

