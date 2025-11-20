package com.example.asmproject.controller.api;

import com.example.asmproject.model.Cart;
import com.example.asmproject.service.CartService;
import com.example.asmproject.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các API liên quan đến giỏ hàng
 * Bao gồm: lấy giỏ hàng, thêm sản phẩm, cập nhật số lượng, xóa sản phẩm, đồng bộ giỏ hàng
 * 
 * Tính năng đặc biệt:
 * - Đồng bộ giỏ hàng trên các thiết bị: Giỏ hàng được lưu trên server theo userId
 *   nên khi đăng nhập trên bất kỳ thiết bị nào, giỏ hàng sẽ tự động đồng bộ
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    /**
     * API lấy danh sách sản phẩm trong giỏ hàng của user
     * Giỏ hàng được sắp xếp theo thời gian thêm mới (mới nhất trước)
     * 
     * Giỏ hàng tự động đồng bộ trên các thiết bị vì:
     * - Tất cả dữ liệu được lưu trên server theo userId
     * - Khi đăng nhập trên thiết bị mới, gọi API này sẽ lấy được giỏ hàng từ server
     * 
     * Chỉ user đã đăng nhập mới có thể xem giỏ hàng của chính mình
     * 
     * @param userId ID của người dùng (lấy từ session hoặc JWT token)
     * @return Danh sách sản phẩm trong giỏ hàng
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @securityUtil.isOwnerOrAdmin(#userId))")
    public ResponseEntity<List<Cart>> getUserCart(@PathVariable Long userId) {
        // Gọi service để lấy giỏ hàng
        // Service sẽ query từ database theo userId
        // Vì dữ liệu lưu trên server nên đồng bộ tự động giữa các thiết bị
        List<Cart> cartItems = cartService.getUserCart(userId);
        return ResponseEntity.ok(cartItems);
    }
    
    /**
     * API thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm (với màu sắc) đã có trong giỏ thì cộng dồn số lượng
     * 
     * Đồng bộ: Khi thêm sản phẩm, dữ liệu được lưu trên server
     * nên các thiết bị khác sẽ thấy ngay khi refresh giỏ hàng
     * 
     * Chỉ user đã đăng nhập mới có thể thêm sản phẩm vào giỏ hàng của chính mình
     * 
     * @param request Chứa: userId, productId, colorId (optional), quantity
     * @return JSON response với thông báo và thông tin cart item
     */
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin từ request
            Long userId = request.get("userId") != null 
                ? Long.valueOf(request.get("userId").toString()) 
                : securityUtil.getCurrentUserId();
            
            // Nếu không có userId thì lấy từ user hiện tại
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Bạn phải đăng nhập để thêm sản phẩm vào giỏ hàng.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra user chỉ có thể thêm vào giỏ hàng của chính mình
            Long currentUserId = securityUtil.getCurrentUserId();
            if (!securityUtil.isAdmin() && !userId.equals(currentUserId)) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền thêm sản phẩm vào giỏ hàng của người khác.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long productId = Long.valueOf(request.get("productId").toString());
            // Màu sắc là optional, có thể null nếu sản phẩm không có màu
            Long colorId = request.get("colorId") != null 
                ? Long.valueOf(request.get("colorId").toString()) : null;
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
            // Validate số lượng
            if (quantity == null || quantity <= 0) {
                response.put("success", false);
                response.put("message", "Số lượng phải lớn hơn 0.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gọi service để thêm vào giỏ hàng
            // Service sẽ:
            // - Kiểm tra sản phẩm có tồn tại và còn hàng không
            // - Nếu đã có trong giỏ (cùng product + color) thì cộng dồn số lượng
            // - Nếu chưa có thì tạo cart item mới
            // - Lưu vào database (server-side) để đồng bộ giữa các thiết bị
            Cart cart = cartService.addToCart(userId, productId, colorId, quantity);
            
            response.put("success", true);
            response.put("message", "Đã thêm vào giỏ hàng.");
            response.put("cart", cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API cập nhật số lượng sản phẩm trong giỏ hàng
     * 
     * @param cartId ID của cart item cần cập nhật
     * @param request Chứa quantity (số lượng mới)
     * @return JSON response với thông báo và thông tin cart item đã cập nhật
     */
    @PutMapping("/{cartId}")
    public ResponseEntity<Map<String, Object>> updateCartQuantity(@PathVariable Long cartId,
                                                                   @RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer quantity = request.get("quantity");
            
            // Validate số lượng
            if (quantity == null) {
                response.put("success", false);
                response.put("message", "Số lượng không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Nếu số lượng = 0 thì xóa khỏi giỏ hàng
            // Nếu số lượng > 0 thì cập nhật
            Cart cart = cartService.updateCartQuantity(cartId, quantity);
            
            response.put("success", true);
            response.put("message", "Cập nhật giỏ hàng thành công.");
            response.put("cart", cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API xóa một sản phẩm khỏi giỏ hàng
     * 
     * @param cartId ID của cart item cần xóa
     * @return JSON response với thông báo kết quả
     */
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long cartId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi service để xóa cart item
            // Service sẽ xóa từ database
            cartService.removeFromCart(cartId);
            
            response.put("success", true);
            response.put("message", "Đã xóa khỏi giỏ hàng.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API xóa toàn bộ giỏ hàng của user
     * 
     * @param userId ID của người dùng
     * @return JSON response với thông báo kết quả
     */
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Map<String, Object>> clearCart(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi service để xóa tất cả cart items của user
            // Service sẽ xóa tất cả từ database
            cartService.clearCart(userId);
            
            response.put("success", true);
            response.put("message", "Đã xóa toàn bộ giỏ hàng.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API lấy số lượng sản phẩm trong giỏ hàng
     * Dùng để hiển thị badge số lượng trên icon giỏ hàng
     * 
     * @param userId ID của người dùng
     * @return JSON response chứa số lượng sản phẩm
     */
    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Long>> getCartItemCount(@PathVariable Long userId) {
        // Đếm số lượng cart items của user
        long count = cartService.getCartItemCount(userId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * API đồng bộ giỏ hàng từ nhiều thiết bị
     * Cho phép client gửi danh sách sản phẩm từ thiết bị local để merge với giỏ hàng trên server
     * 
     * Logic:
     * - Nếu sản phẩm (product + color) đã có trên server: cộng dồn số lượng
     * - Nếu chưa có: thêm mới vào giỏ hàng
     * 
     * Use case: User thêm sản phẩm vào giỏ hàng khi chưa đăng nhập (local storage),
     * sau khi đăng nhập thì gọi API này để đồng bộ lên server
     * 
     * @param userId ID của người dùng
     * @param request Chứa danh sách sản phẩm từ thiết bị local: 
     *                [{productId, colorId, quantity}, ...]
     * @return JSON response với danh sách giỏ hàng đã được đồng bộ
     */
    @PostMapping("/sync/{userId}")
    public ResponseEntity<Map<String, Object>> syncCart(@PathVariable Long userId,
                                                         @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy danh sách sản phẩm từ thiết bị local
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> localCartItems = (List<Map<String, Object>>) request.get("items");
            
            if (localCartItems == null || localCartItems.isEmpty()) {
                // Nếu không có sản phẩm local thì chỉ trả về giỏ hàng trên server
                List<Cart> serverCart = cartService.getUserCart(userId);
                response.put("success", true);
                response.put("message", "Đồng bộ giỏ hàng thành công.");
                response.put("cart", serverCart);
                return ResponseEntity.ok(response);
            }
            
            // Đồng bộ từng sản phẩm
            // Service sẽ tự động merge với giỏ hàng trên server
            List<Cart> syncedCart = cartService.syncCart(userId, localCartItems);
            
            response.put("success", true);
            response.put("message", "Đồng bộ giỏ hàng thành công.");
            response.put("cart", syncedCart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

