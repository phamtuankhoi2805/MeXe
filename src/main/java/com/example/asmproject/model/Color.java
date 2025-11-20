package com.example.asmproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colors")
public class Color {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Tên màu không được để trống")
    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    @Column(name = "hex_code", length = 7)
    private String hexCode;
    
    @Column(length = 500)
    private String image;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColor> productColors = new ArrayList<>();
    
    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> cartItems = new ArrayList<>();
    
    // Constructors
    public Color() {
    }
    
    public Color(String name, String hexCode) {
        this.name = name;
        this.hexCode = hexCode;
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
    
    public String getHexCode() {
        return hexCode;
    }
    
    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<ProductColor> getProductColors() {
        return productColors;
    }
    
    public void setProductColors(List<ProductColor> productColors) {
        this.productColors = productColors;
    }
    
    public List<Cart> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<Cart> cartItems) {
        this.cartItems = cartItems;
    }
}

