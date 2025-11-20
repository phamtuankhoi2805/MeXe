package com.example.asmproject.service;

import com.example.asmproject.model.Cart;
import com.example.asmproject.model.Color;
import com.example.asmproject.model.Product;
import com.example.asmproject.model.User;
import com.example.asmproject.repository.CartRepository;
import com.example.asmproject.repository.ColorRepository;
import com.example.asmproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ColorRepository colorRepository;
    
    public List<Cart> getUserCart(Long userId) {
        return cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Cart addToCart(Long userId, Long productId, Long colorId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        if (!product.isInStock()) {
            throw new RuntimeException("Sản phẩm hiện không có sẵn");
        }
        
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm không đủ");
        }
        
        Color color = null;
        if (colorId != null) {
            color = colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"));
        }
        
        Optional<Cart> existingCart = colorId != null
            ? cartRepository.findByUserIdAndProductIdAndColorId(userId, productId, colorId)
            : cartRepository.findByUserIdAndProductIdAndColorIdIsNull(userId, productId);
        
        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            cart = new Cart();
            cart.setUser(new User());
            cart.getUser().setId(userId);
            cart.setProduct(product);
            cart.setColor(color);
            cart.setQuantity(quantity);
        }
        
        return cartRepository.save(cart);
    }
    
    public Cart updateCartQuantity(Long cartId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        
        if (quantity <= 0) {
            cartRepository.delete(cart);
            return cart;
        }
        
        Product product = cart.getProduct();
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm không đủ");
        }
        
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }
    
    public void removeFromCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }
    
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    public long getCartItemCount(Long userId) {
        return cartRepository.countByUserId(userId);
    }
}

