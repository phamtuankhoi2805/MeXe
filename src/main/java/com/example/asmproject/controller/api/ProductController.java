package com.example.asmproject.controller.api;

import com.example.asmproject.model.Product;
import com.example.asmproject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các API liên quan đến sản phẩm
 * Bao gồm: tìm kiếm sản phẩm, lấy danh sách sản phẩm, lấy chi tiết sản phẩm
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * API tìm kiếm sản phẩm với nhiều tiêu chí
     * Hỗ trợ tìm kiếm theo: từ khóa, thương hiệu, danh mục, trạng thái
     * Kết quả được phân trang
     * 
     * Tìm kiếm theo từ khóa:
     * - Tìm trong tên sản phẩm (name)
     * - Tìm trong mô tả sản phẩm (description)
     * - Không phân biệt hoa thường (case-insensitive)
     * 
     * @param keyword Từ khóa tìm kiếm (tên sản phẩm hoặc mô tả)
     * @param brandId ID của thương hiệu (optional)
     * @param categoryId ID của danh mục (optional)
     * @param status Trạng thái sản phẩm: ACTIVE, INACTIVE, OUT_OF_STOCK (optional)
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng sản phẩm mỗi trang (mặc định 12)
     * @return Page<Product> - Danh sách sản phẩm đã được phân trang
     */
    @GetMapping
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        // Parse status string thành enum
        // Nếu status không hợp lệ thì bỏ qua (tìm tất cả trạng thái)
        Product.ProductStatus productStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore - tìm tất cả trạng thái
            }
        }
        
        // Tạo Pageable object cho phân trang
        // Page bắt đầu từ 0, size mặc định là 12 sản phẩm mỗi trang
        Pageable pageable = PageRequest.of(page, size);
        
        // Gọi service để tìm kiếm sản phẩm
        // Service sẽ query từ database với các điều kiện:
        // - keyword: tìm trong name hoặc description (LIKE %keyword%)
        // - brandId: filter theo thương hiệu
        // - categoryId: filter theo danh mục
        // - status: filter theo trạng thái
        Page<Product> products = productService.searchProducts(keyword, brandId, categoryId, productStatus, pageable);
        
        return ResponseEntity.ok(products);
    }
    
    /**
     * API lấy danh sách tất cả sản phẩm đang hoạt động
     * Trả về tất cả sản phẩm có status = ACTIVE (không phân trang)
     * 
     * Dùng cho:
     * - Hiển thị danh sách sản phẩm trên trang chủ
     * - Hiển thị danh sách sản phẩm trong category
     * - Client-side filtering/sorting
     * 
     * @return Danh sách tất cả sản phẩm đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        // Gọi service để lấy tất cả sản phẩm có status = ACTIVE
        List<Product> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * API lấy danh sách sản phẩm đang hoạt động với phân trang
     * 
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng sản phẩm mỗi trang (mặc định 12)
     * @return Page<Product> - Danh sách sản phẩm đang hoạt động đã được phân trang
     */
    @GetMapping("/active/paged")
    public ResponseEntity<Page<Product>> getActiveProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        // Tạo Pageable object cho phân trang
        Pageable pageable = PageRequest.of(page, size);
        
        // Gọi service để lấy sản phẩm đang hoạt động với phân trang
        Page<Product> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    /**
     * API lấy chi tiết sản phẩm theo ID
     * 
     * @param id ID của sản phẩm
     * @return Product object nếu tìm thấy, 404 nếu không tìm thấy
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // Tìm sản phẩm theo ID
        Optional<Product> product = productService.getProductById(id);
        
        // Nếu tìm thấy thì trả về, nếu không thì trả về 404
        return product.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * API lấy chi tiết sản phẩm theo slug (URL-friendly name)
     * Slug được dùng trong URL thay vì ID để SEO tốt hơn
     * 
     * Ví dụ: /api/products/slug/vinfast-klara-a1 thay vì /api/products/123
     * 
     * @param slug Slug của sản phẩm (URL-friendly name)
     * @return Product object nếu tìm thấy, 404 nếu không tìm thấy
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        // Tìm sản phẩm theo slug
        Optional<Product> product = productService.getProductBySlug(slug);
        
        // Nếu tìm thấy thì trả về, nếu không thì trả về 404
        return product.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * API tìm kiếm nâng cao với thông tin chi tiết hơn
     * Trả về thêm thông tin như: tổng số kết quả, số trang, ...
     * 
     * @param keyword Từ khóa tìm kiếm
     * @param brandId ID của thương hiệu
     * @param categoryId ID của danh mục
     * @param status Trạng thái sản phẩm
     * @param page Số trang
     * @param size Số lượng sản phẩm mỗi trang
     * @return JSON response với thông tin chi tiết về kết quả tìm kiếm
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProductsWithDetails(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Parse status
        Product.ProductStatus productStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        // Tạo Pageable
        Pageable pageable = PageRequest.of(page, size);
        
        // Tìm kiếm sản phẩm
        Page<Product> products = productService.searchProducts(keyword, brandId, categoryId, productStatus, pageable);
        
        // Tạo response với thông tin chi tiết
        response.put("products", products.getContent());
        response.put("totalElements", products.getTotalElements()); // Tổng số sản phẩm
        response.put("totalPages", products.getTotalPages()); // Tổng số trang
        response.put("currentPage", products.getNumber()); // Trang hiện tại
        response.put("pageSize", products.getSize()); // Số lượng sản phẩm mỗi trang
        response.put("hasNext", products.hasNext()); // Có trang tiếp theo không
        response.put("hasPrevious", products.hasPrevious()); // Có trang trước không
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API lấy gợi ý tìm kiếm (autocomplete)
     * Trả về danh sách sản phẩm gợi ý dựa trên từ khóa
     * Giới hạn 5 kết quả để hiển thị nhanh
     * 
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách sản phẩm gợi ý (tối đa 5)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<Map<String, Object>>> getSearchSuggestions(
            @RequestParam(required = false) String keyword) {
        
        List<Map<String, Object>> suggestions = new java.util.ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty() && keyword.trim().length() >= 2) {
            // Tìm kiếm với giới hạn 5 kết quả
            Pageable pageable = PageRequest.of(0, 5);
            Page<Product> products = productService.searchProducts(
                keyword.trim(), 
                null, 
                null, 
                Product.ProductStatus.ACTIVE, 
                pageable
            );
            
            // Chuyển đổi sang format đơn giản cho autocomplete
            for (Product product : products.getContent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getId());
                item.put("name", product.getName());
                item.put("slug", product.getSlug());
                item.put("image", product.getImage());
                item.put("price", product.getFinalPrice());
                suggestions.add(item);
            }
        }
        
        return ResponseEntity.ok(suggestions);
    }
}

