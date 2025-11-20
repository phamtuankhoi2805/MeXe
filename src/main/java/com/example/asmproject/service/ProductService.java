package com.example.asmproject.service;

import com.example.asmproject.model.Product;
import com.example.asmproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public Page<Product> searchProducts(String keyword, Long brandId, Long categoryId, 
                                       Product.ProductStatus status, Pageable pageable) {
        return productRepository.searchProducts(keyword, brandId, categoryId, status, pageable);
    }
    
    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE);
    }
    
    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable);
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    public List<Product> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    public long getTotalActiveProducts() {
        return productRepository.countActiveProducts();
    }
    
    public long getTotalOutOfStockProducts() {
        return productRepository.countOutOfStockProducts();
    }
}

