package com.example.asmproject.repository;

import com.example.asmproject.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    List<Cart> findByUserId(Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Cart> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    Optional<Cart> findByUserIdAndProductIdAndColorId(Long userId, Long productId, Long colorId);
    
    Optional<Cart> findByUserIdAndProductIdAndColorIdIsNull(Long userId, Long productId);
    
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}

