package com.example.asmproject.repository;

import com.example.asmproject.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySlug(String slug);
    
    List<Product> findByStatus(Product.ProductStatus status);
    
    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(
        @Param("keyword") String keyword,
        @Param("brandId") Long brandId,
        @Param("categoryId") Long categoryId,
        @Param("status") Product.ProductStatus status,
        Pageable pageable
    );
    
    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId")
    List<Product> findByBrandId(@Param("brandId") Long brandId);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'")
    long countActiveProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity <= 0")
    long countOutOfStockProducts();
    
    /**
     * Lấy sản phẩm theo slug với JOIN FETCH để load brand, category và productColors với Color
     * Tránh lazy loading exception
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.productColors pc " +
           "LEFT JOIN FETCH pc.color " +
           "WHERE p.slug = :slug")
    Optional<Product> findBySlugWithBrandAndCategory(@Param("slug") String slug);
}

