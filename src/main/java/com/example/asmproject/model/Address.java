package com.example.asmproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "Họ tên người nhận không được để trống")
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(nullable = false, length = 20)
    private String phone;
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Column(nullable = false, length = 100)
    private String province;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    @Column(nullable = false, length = 100)
    private String district;
    
    @NotBlank(message = "Phường/Xã không được để trống")
    @Column(nullable = false, length = 100)
    private String ward;
    
    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Column(nullable = false, length = 500)
    private String street;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "address")
    private java.util.List<Order> orders;
    
    // Constructors
    public Address() {
    }
    
    public Address(User user, String fullName, String phone, String province, 
                   String district, String ward, String street) {
        this.user = user;
        this.fullName = fullName;
        this.phone = phone;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.street = street;
    }
    
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", street, ward, district, province);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getWard() {
        return ward;
    }
    
    public void setWard(String ward) {
        this.ward = ward;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
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
    
    public java.util.List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(java.util.List<Order> orders) {
        this.orders = orders;
    }
}

