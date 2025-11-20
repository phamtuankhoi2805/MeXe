package com.example.asmproject.model;

import java.io.Serializable;
import java.util.Objects;

public class ProductColorId implements Serializable {
    private Long product;
    private Long color;
    
    public ProductColorId() {
    }
    
    public ProductColorId(Long product, Long color) {
        this.product = product;
        this.color = color;
    }
    
    public Long getProduct() {
        return product;
    }
    
    public void setProduct(Long product) {
        this.product = product;
    }
    
    public Long getColor() {
        return color;
    }
    
    public void setColor(Long color) {
        this.color = color;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductColorId that = (ProductColorId) o;
        return Objects.equals(product, that.product) && Objects.equals(color, that.color);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(product, color);
    }
}

