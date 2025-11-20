package com.example.asmproject.controller.api;

import com.example.asmproject.model.Cart;
import com.example.asmproject.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Cart>> getUserCart(@PathVariable Long userId) {
        List<Cart> cartItems = cartService.getUserCart(userId);
        return ResponseEntity.ok(cartItems);
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long productId = Long.valueOf(request.get("productId").toString());
            Long colorId = request.get("colorId") != null 
                ? Long.valueOf(request.get("colorId").toString()) : null;
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
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
    
    @PutMapping("/{cartId}")
    public ResponseEntity<Map<String, Object>> updateCartQuantity(@PathVariable Long cartId,
                                                                   @RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer quantity = request.get("quantity");
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
    
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long cartId) {
        Map<String, Object> response = new HashMap<>();
        try {
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
    
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Map<String, Object>> clearCart(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
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
    
    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Long>> getCartItemCount(@PathVariable Long userId) {
        long count = cartService.getCartItemCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}

