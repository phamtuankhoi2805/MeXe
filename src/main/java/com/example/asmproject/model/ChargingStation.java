package com.example.asmproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_stations")
public class ChargingStation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Tên trạm không được để trống")
    @Column(nullable = false, length = 255)
    private String name;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    @Column(nullable = false, length = 500)
    private String address;
    
    @NotNull(message = "Vĩ độ không được để trống")
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @NotNull(message = "Kinh độ không được để trống")
    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "available_batteries", nullable = false)
    private Integer availableBatteries = 0;
    
    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity = 0;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "operating_hours", length = 100)
    private String operatingHours = "24/7";
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StationStatus status = StationStatus.ACTIVE;
    
    @Column(name = "province", length = 100)
    private String province;
    
    @Column(name = "district", length = 100)
    private String district;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum StationStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
    
    // Constructors
    public ChargingStation() {
    }
    
    public ChargingStation(String name, String address, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Helper method: Tính khoảng cách đến một điểm (Haversine formula)
    public double calculateDistance(BigDecimal lat, BigDecimal lng) {
        if (latitude == null || longitude == null || lat == null || lng == null) {
            return Double.MAX_VALUE;
        }
        
        double lat1 = latitude.doubleValue();
        double lon1 = longitude.doubleValue();
        double lat2 = lat.doubleValue();
        double lon2 = lng.doubleValue();
        
        final int R = 6371; // Bán kính Trái Đất tính bằng km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        return distance;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    
    public Integer getAvailableBatteries() {
        return availableBatteries;
    }
    
    public void setAvailableBatteries(Integer availableBatteries) {
        this.availableBatteries = availableBatteries;
    }
    
    public Integer getTotalCapacity() {
        return totalCapacity;
    }
    
    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getOperatingHours() {
        return operatingHours;
    }
    
    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }
    
    public StationStatus getStatus() {
        return status;
    }
    
    public void setStatus(StationStatus status) {
        this.status = status;
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
}

