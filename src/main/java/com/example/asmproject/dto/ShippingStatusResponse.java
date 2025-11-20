package com.example.asmproject.dto;

import com.example.asmproject.model.enums.ShippingStatus;
import com.example.asmproject.model.enums.ShippingType;
import java.time.LocalDateTime;

public class ShippingStatusResponse {

    private String orderCode;
    private ShippingType shippingType;
    private ShippingStatus shippingStatus;
    private LocalDateTime lastUpdated;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public ShippingType getShippingType() {
        return shippingType;
    }

    public void setShippingType(ShippingType shippingType) {
        this.shippingType = shippingType;
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(ShippingStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}




