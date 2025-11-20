package com.example.asmproject.service;

import com.example.asmproject.model.Cart;
import com.example.asmproject.model.Color;
import com.example.asmproject.model.Product;
import com.example.asmproject.model.User;
import com.example.asmproject.repository.CartRepository;
import com.example.asmproject.repository.ColorRepository;
import com.example.asmproject.repository.ProductRepository;
import com.example.asmproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service xử lý các logic nghiệp vụ liên quan đến giỏ hàng
 * Bao gồm: thêm, sửa, xóa sản phẩm trong giỏ hàng, đồng bộ giỏ hàng
 * 
 * Tính năng đặc biệt:
 * - Đồng bộ giỏ hàng trên các thiết bị: Tất cả dữ liệu được lưu trên server theo userId
 *   nên khi đăng nhập trên thiết bị mới, giỏ hàng tự động đồng bộ
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ColorRepository colorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy danh sách sản phẩm trong giỏ hàng của user
     * Sắp xếp theo thời gian thêm mới (mới nhất trước)
     * 
     * @param userId ID của người dùng
     * @return Danh sách sản phẩm trong giỏ hàng
     */
    public List<Cart> getUserCart(Long userId) {
        // Query từ database theo userId
        // Vì dữ liệu lưu trên server nên đồng bộ tự động giữa các thiết bị
        return cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm (với màu sắc) đã có trong giỏ thì cộng dồn số lượng
     * 
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorId ID của màu sắc (optional, null nếu sản phẩm không có màu)
     * @param quantity Số lượng muốn thêm
     * @return Cart item đã được lưu vào database
     * @throws RuntimeException nếu sản phẩm không tồn tại, hết hàng hoặc số lượng không đủ
     */
    public Cart addToCart(Long userId, Long productId, Long colorId, Integer quantity) {
        // Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        // Kiểm tra sản phẩm còn hàng không
        if (!product.isInStock()) {
            throw new RuntimeException("Sản phẩm hiện không có sẵn. Vui lòng thử lại sau.");
        }
        
        // Kiểm tra số lượng có đủ không
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm không đủ. Hiện còn " + product.getQuantity() + " sản phẩm.");
        }
        
        // Lấy thông tin màu sắc nếu có
        Color color = null;
        if (colorId != null) {
            color = colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"));
        }
        
        // Kiểm tra sản phẩm (với màu sắc) đã có trong giỏ hàng chưa
        // Nếu có thì cộng dồn số lượng, nếu chưa có thì tạo mới
        Optional<Cart> existingCart = colorId != null
            ? cartRepository.findByUserIdAndProductIdAndColorId(userId, productId, colorId)
            : cartRepository.findByUserIdAndProductIdAndColorIdIsNull(userId, productId);
        
        Cart cart;
        if (existingCart.isPresent()) {
            // Đã có trong giỏ hàng -> cộng dồn số lượng
            cart = existingCart.get();
            int newQuantity = cart.getQuantity() + quantity;
            
            // Kiểm tra tổng số lượng không vượt quá số lượng trong kho
            if (newQuantity > product.getQuantity()) {
                throw new RuntimeException("Số lượng trong giỏ hàng không được vượt quá số lượng trong kho. Hiện còn " + product.getQuantity() + " sản phẩm.");
            }
            
            cart.setQuantity(newQuantity);
        } else {
            // Chưa có trong giỏ hàng -> tạo mới
            cart = new Cart();
            // Lấy user từ database (không nên tạo user mới)
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            cart.setUser(user);
            cart.setProduct(product);
            cart.setColor(color);
            cart.setQuantity(quantity);
        }
        
        // Lưu vào database (server-side)
        // Vì dữ liệu lưu trên server nên đồng bộ tự động giữa các thiết bị
        return cartRepository.save(cart);
    }
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * Nếu số lượng = 0 thì tự động xóa khỏi giỏ hàng
     * 
     * @param cartId ID của cart item cần cập nhật
     * @param quantity Số lượng mới
     * @return Cart item đã được cập nhật (hoặc đã xóa)
     * @throws RuntimeException nếu cart không tồn tại hoặc số lượng không đủ
     */
    public Cart updateCartQuantity(Long cartId, Integer quantity) {
        // Tìm cart item theo ID
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        
        // Nếu số lượng <= 0 thì xóa khỏi giỏ hàng
        if (quantity <= 0) {
            cartRepository.delete(cart);
            return cart;
        }
        
        // Kiểm tra số lượng không vượt quá số lượng trong kho
        Product product = cart.getProduct();
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm không đủ. Hiện còn " + product.getQuantity() + " sản phẩm.");
        }
        
        // Cập nhật số lượng
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }
    
    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     * 
     * @param cartId ID của cart item cần xóa
     */
    public void removeFromCart(Long cartId) {
        // Xóa từ database
        cartRepository.deleteById(cartId);
    }
    
    /**
     * Xóa toàn bộ giỏ hàng của user
     * 
     * @param userId ID của người dùng
     */
    public void clearCart(Long userId) {
        // Xóa tất cả cart items của user từ database
        cartRepository.deleteByUserId(userId);
    }
    
    /**
     * Đếm số lượng sản phẩm trong giỏ hàng của user
     * Dùng để hiển thị badge số lượng trên icon giỏ hàng
     * 
     * @param userId ID của người dùng
     * @return Số lượng sản phẩm trong giỏ hàng
     */
    public long getCartItemCount(Long userId) {
        return cartRepository.countByUserId(userId);
    }
    
    /**
     * Đồng bộ giỏ hàng từ thiết bị local lên server
     * Merge giỏ hàng local với giỏ hàng trên server
     * 
     * Logic:
     * 1. Lấy giỏ hàng trên server (theo userId)
     * 2. Với mỗi sản phẩm từ thiết bị local:
     *    - Nếu sản phẩm (product + color) đã có trên server: cộng dồn số lượng
     *    - Nếu chưa có: thêm mới vào giỏ hàng
     * 3. Trả về danh sách giỏ hàng đã được đồng bộ
     * 
     * Use case: User thêm sản phẩm vào giỏ hàng khi chưa đăng nhập (lưu local storage),
     * sau khi đăng nhập thì gọi API này để đồng bộ lên server
     * 
     * @param userId ID của người dùng
     * @param localCartItems Danh sách sản phẩm từ thiết bị local: 
     *                       [{productId, colorId, quantity}, ...]
     * @return Danh sách giỏ hàng đã được đồng bộ
     */
    public List<Cart> syncCart(Long userId, List<Map<String, Object>> localCartItems) {
        // Lấy giỏ hàng hiện tại trên server
        List<Cart> serverCart = getUserCart(userId);
        
        // Đồng bộ từng sản phẩm từ thiết bị local
        for (Map<String, Object> item : localCartItems) {
            try {
                Long productId = Long.valueOf(item.get("productId").toString());
                Long colorId = item.get("colorId") != null 
                    ? Long.valueOf(item.get("colorId").toString()) : null;
                Integer quantity = Integer.valueOf(item.get("quantity").toString());
                
                if (quantity == null || quantity <= 0) {
                    continue; // Bỏ qua nếu số lượng không hợp lệ
                }
                
                // Kiểm tra sản phẩm này đã có trong giỏ hàng trên server chưa
                boolean existsInServer = false;
                Long existingCartId = null;
                
                for (Cart cart : serverCart) {
                    boolean sameProduct = cart.getProduct().getId().equals(productId);
                    boolean sameColor = (colorId == null && cart.getColor() == null) ||
                                       (colorId != null && cart.getColor() != null && 
                                        cart.getColor().getId().equals(colorId));
                    
                    if (sameProduct && sameColor) {
                        existsInServer = true;
                        existingCartId = cart.getId();
                        break;
                    }
                }
                
                if (existsInServer && existingCartId != null) {
                    // Đã có trong giỏ hàng trên server -> cộng dồn số lượng
                    Cart existingCart = cartRepository.findById(existingCartId)
                        .orElse(null);
                    if (existingCart != null) {
                        int newQuantity = existingCart.getQuantity() + quantity;
                        updateCartQuantity(existingCartId, newQuantity);
                    }
                } else {
                    // Chưa có trong giỏ hàng trên server -> thêm mới
                    try {
                        addToCart(userId, productId, colorId, quantity);
                    } catch (Exception e) {
                        // Bỏ qua nếu sản phẩm không tồn tại hoặc hết hàng
                        // Log error nếu cần: System.out.println("Error syncing cart item: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // Bỏ qua item không hợp lệ và tiếp tục với item tiếp theo
                // Log error nếu cần: System.out.println("Error processing cart item: " + e.getMessage());
                continue;
            }
        }
        
        // Trả về giỏ hàng đã được đồng bộ (refresh từ database)
        return getUserCart(userId);
    }
}

