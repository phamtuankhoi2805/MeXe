package com.example.asmproject.controller.api.admin;

import com.example.asmproject.model.Product;
import com.example.asmproject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    
    @Autowired
    private ProductService productService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product saved = productService.saveProduct(product);
            response.put("success", true);
            response.put("message", "Tạo sản phẩm thành công.");
            response.put("product", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id,
                                                             @RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Product> existing = productService.getProductById(id);
            if (existing.isEmpty()) {
                response.put("success", false);
                response.put("message", "Sản phẩm không tồn tại.");
                return ResponseEntity.notFound().build();
            }
            
            product.setId(id);
            Product updated = productService.saveProduct(product);
            response.put("success", true);
            response.put("message", "Cập nhật sản phẩm thành công.");
            response.put("product", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(id);
            response.put("success", true);
            response.put("message", "Xóa sản phẩm thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

