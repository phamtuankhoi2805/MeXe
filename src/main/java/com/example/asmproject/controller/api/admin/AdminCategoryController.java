package com.example.asmproject.controller.api.admin;

import com.example.asmproject.model.Category;
import com.example.asmproject.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý Danh mục (Category).
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Lấy tất cả danh mục sản phẩm.
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Lấy chi tiết danh mục.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Tạo danh mục mới.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        try {
            Category saved = categoryService.saveCategory(category);
            response.put("success", true);
            response.put("message", "Tạo danh mục thành công.");
            response.put("category", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật danh mục.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
                                                              @RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        try {
            category.setId(id);
            Category updated = categoryService.saveCategory(category);
            response.put("success", true);
            response.put("message", "Cập nhật danh mục thành công.");
            response.put("category", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa danh mục.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            categoryService.deleteCategory(id);
            response.put("success", true);
            response.put("message", "Xóa danh mục thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

