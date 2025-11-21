package com.example.asmproject.controller.api.admin;

import com.example.asmproject.dto.ProductRequest;
import com.example.asmproject.dto.ProductResponse;
import com.example.asmproject.model.Product;
import com.example.asmproject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý sản phẩm (Admin).
 * Cung cấp đầy đủ các chức năng CRUD và tìm kiếm sản phẩm.
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    
    @Autowired
    private ProductService productService;

    /**
     * Tìm kiếm sản phẩm theo nhiều tiêu chí (tên, thương hiệu, danh mục, trạng thái).
     * Kết quả trả về được phân trang.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Product.ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // Tạo pageable để phân trang, mặc định 20 sản phẩm/trang
        PageRequest pageable = PageRequest.of(page, size);
        
        // Gọi service để tìm kiếm
        Page<Product> products = productService.searchProducts(keyword, brandId, categoryId, status, pageable);
        
        // Convert sang DTO response để trả về
        Page<ProductResponse> responses = products.map(productService::toProductResponse);
        return ResponseEntity.ok(responses);
    }

    /**
     * Lấy chi tiết thông tin sản phẩm theo ID.
     * Dùng để hiển thị form sửa sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(productService::toProductResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Tạo mới một sản phẩm.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductResponse created = productService.createProduct(request);
            response.put("success", true);
            response.put("message", "Tạo sản phẩm thành công.");
            response.put("product", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Bắt lỗi validation hoặc logic nghiệp vụ
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật thông tin sản phẩm.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id,
                                                             @RequestBody ProductRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductResponse updated = productService.updateProduct(id, request);
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
    
    /**
     * Xóa (hoặc ẩn) sản phẩm.
     * Lưu ý: Logic thực tế trong service có thể chỉ là soft-delete (đổi trạng thái).
     */
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

