package com.example.asmproject.controller.api.admin;

import com.example.asmproject.model.Brand;
import com.example.asmproject.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý Thương hiệu (Brand).
 */
@RestController
@RequestMapping("/api/admin/brands")
public class AdminBrandController {

    @Autowired
    private BrandService brandService;

    /**
     * Lấy tất cả thương hiệu (dùng cho combobox chọn thương hiệu khi tạo sản phẩm).
     */
    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    /**
     * Lấy chi tiết một thương hiệu.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        return brandService.getBrandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Thêm mới thương hiệu.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBrand(@RequestBody Brand brand) {
        Map<String, Object> response = new HashMap<>();
        try {
            Brand saved = brandService.saveBrand(brand);
            response.put("success", true);
            response.put("message", "Tạo thương hiệu thành công.");
            response.put("brand", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật thông tin thương hiệu.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBrand(@PathVariable Long id,
            @RequestBody Brand brand) {
        Map<String, Object> response = new HashMap<>();
        try {
            brand.setId(id);
            Brand updated = brandService.saveBrand(brand);
            response.put("success", true);
            response.put("message", "Cập nhật thương hiệu thành công.");
            response.put("brand", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa thương hiệu.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBrand(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            brandService.deleteBrand(id);
            response.put("success", true);
            response.put("message", "Xóa thương hiệu thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
