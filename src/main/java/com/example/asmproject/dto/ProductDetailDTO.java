package com.example.asmproject.dto;

import com.example.asmproject.model.Product;
import java.math.BigDecimal;

/**
 * DTO cho trang chi tiết sản phẩm
 * Tránh lazy loading exception bằng cách load tất cả dữ liệu cần thiết
 */
public class ProductDetailDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal finalPrice;
    private Integer quantity;
    private String brandName;
    private String categoryName;
    private String image;
    private String images;
    private String specifications;
    private Product.ProductStatus status;
    
    // Constructors
    public ProductDetailDTO() {
    }
    
    public ProductDetailDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.slug = product.getSlug();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.discountPrice = product.getDiscountPrice();
        this.finalPrice = product.getFinalPrice();
        this.quantity = product.getQuantity();
        this.brandName = product.getBrand() != null ? product.getBrand().getName() : null;
        this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        this.image = product.getImage();
        this.images = product.getImages();
        this.specifications = product.getSpecifications();
        this.status = product.getStatus();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }
    
    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }
    
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
    
    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public String getSpecifications() {
        return specifications;
    }
    
    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
    
    public Product.ProductStatus getStatus() {
        return status;
    }
    
    public void setStatus(Product.ProductStatus status) {
        this.status = status;
    }
}

