package com.example.asmproject.repository;

import com.example.asmproject.model.ProductColor;
import com.example.asmproject.model.ProductColorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, ProductColorId> {
    
    List<ProductColor> findByProductId(Long productId);
    
    Optional<ProductColor> findByProductIdAndColorId(Long productId, Long colorId);
    
    void deleteByProductId(Long productId);
}

