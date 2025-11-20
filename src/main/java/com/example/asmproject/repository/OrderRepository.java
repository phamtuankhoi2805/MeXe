package com.example.asmproject.repository;

import com.example.asmproject.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    Optional<Order> findByOrderCode(String orderCode);
    
    List<Order> findByUserId(Long userId);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "o.orderCode LIKE CONCAT('%', :keyword, '%') OR " +
           "o.user.email LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:orderStatus IS NULL OR o.orderStatus = :orderStatus) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:deliveryMethod IS NULL OR o.deliveryMethod = :deliveryMethod)")
    Page<Order> searchOrders(
        @Param("keyword") String keyword,
        @Param("orderStatus") Order.OrderStatus orderStatus,
        @Param("paymentStatus") Order.PaymentStatus paymentStatus,
        @Param("deliveryMethod") Order.DeliveryMethod deliveryMethod,
        Pageable pageable
    );
    
    @Query("SELECT o FROM Order o WHERE o.deliveryMethod = 'FAST'")
    List<Order> findFastDeliveryOrders();
    
    @Query("SELECT o FROM Order o WHERE o.deliveryMethod = 'FAST' AND o.orderStatus = :status")
    List<Order> findFastDeliveryOrdersByStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
    long countByOrderStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :status")
    long countByPaymentStatus(@Param("status") Order.PaymentStatus status);
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderStatus = 'DELIVERED' AND o.createdAt >= :startDate")
    Double sumTotalByDeliveredAndDateAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.address.id = :addressId")
    long countByAddressId(@Param("addressId") Long addressId);
}

