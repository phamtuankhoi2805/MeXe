package com.example.asmproject.config;

import com.example.asmproject.model.Category;
import com.example.asmproject.model.Product;
import com.example.asmproject.service.CategoryService;
import com.example.asmproject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Controller Advice để tự động load dữ liệu vào mọi view
 * Đảm bảo navigation menu luôn có dữ liệu sản phẩm từ database
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Tự động load sản phẩm theo category vào navigation menu
     * Sản phẩm được nhóm theo 3 category: CAO CẤP, TRUNG CẤP, PHỔ THÔNG
     */
    @ModelAttribute("navProducts")
    public Map<String, List<Product>> getNavProducts() {
        Map<String, List<Product>> navProducts = new HashMap<>();
        
        // Lấy tất cả category
        List<Category> categories = categoryService.getAllCategories();
        
        // Tạo map category name -> products
        Map<String, List<Product>> categoryProductsMap = new HashMap<>();
        
        for (Category category : categories) {
            String categoryName = category.getName();
            List<Product> products = productService.getProductsByCategory(category.getId())
                    .stream()
                    .filter(p -> p.getStatus() == Product.ProductStatus.ACTIVE)
                    .limit(10) // Giới hạn tối đa 10 sản phẩm mỗi category
                    .collect(Collectors.toList());
            
            categoryProductsMap.put(categoryName, products);
        }
        
        // Map theo tên category trong navigation
        // CAO CẤP
        navProducts.put("caoCap", categoryProductsMap.getOrDefault("CAO CẤP", 
                categoryProductsMap.values().stream()
                        .flatMap(List::stream)
                        .filter(p -> p.getPrice().doubleValue() >= 50000000) // Giá >= 50 triệu
                        .limit(10)
                        .collect(Collectors.toList())));
        
        // TRUNG CẤP
        navProducts.put("trungCap", categoryProductsMap.getOrDefault("TRUNG CẤP",
                categoryProductsMap.values().stream()
                        .flatMap(List::stream)
                        .filter(p -> {
                            double price = p.getPrice().doubleValue();
                            return price >= 20000000 && price < 50000000; // 20-50 triệu
                        })
                        .limit(10)
                        .collect(Collectors.toList())));
        
        // PHỔ THÔNG
        navProducts.put("phoThong", categoryProductsMap.getOrDefault("PHỔ THÔNG",
                categoryProductsMap.values().stream()
                        .flatMap(List::stream)
                        .filter(p -> p.getPrice().doubleValue() < 20000000) // < 20 triệu
                        .limit(10)
                        .collect(Collectors.toList())));
        
        // Nếu không tìm thấy category theo tên, thử tìm theo pattern
        if (navProducts.get("caoCap").isEmpty()) {
            for (Map.Entry<String, List<Product>> entry : categoryProductsMap.entrySet()) {
                String name = entry.getKey().toUpperCase();
                if (name.contains("CAO") || name.contains("PREMIUM") || name.contains("LUXURY")) {
                    navProducts.put("caoCap", entry.getValue());
                    break;
                }
            }
        }
        
        if (navProducts.get("trungCap").isEmpty()) {
            for (Map.Entry<String, List<Product>> entry : categoryProductsMap.entrySet()) {
                String name = entry.getKey().toUpperCase();
                if (name.contains("TRUNG") || name.contains("MID") || name.contains("MIDDLE")) {
                    navProducts.put("trungCap", entry.getValue());
                    break;
                }
            }
        }
        
        if (navProducts.get("phoThong").isEmpty()) {
            for (Map.Entry<String, List<Product>> entry : categoryProductsMap.entrySet()) {
                String name = entry.getKey().toUpperCase();
                if (name.contains("PHỔ") || name.contains("THÔNG") || name.contains("POPULAR") || name.contains("BASIC")) {
                    navProducts.put("phoThong", entry.getValue());
                    break;
                }
            }
        }
        
        return navProducts;
    }
}

