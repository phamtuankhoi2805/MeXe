package com.example.asmproject.repository;

import com.example.asmproject.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);

    Page<Voucher> findByStatus(Voucher.VoucherStatus status, Pageable pageable);

    /**
     * Tìm voucher active theo code
     */
    @Query("SELECT v FROM Voucher v WHERE v.code = :code " +
           "AND v.status = 'ACTIVE' " +
           "AND v.startDate <= CURRENT_TIMESTAMP " +
           "AND v.endDate >= CURRENT_TIMESTAMP " +
           "AND v.usedCount < v.quantity")
    Optional<Voucher> findActiveByCode(@Param("code") String code);
    
    /**
     * Tìm các voucher còn hạn sử dụng, còn số lượng và đang active
     */
    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' " +
           "AND v.startDate <= :now " +
           "AND v.endDate >= :now " +
           "AND v.usedCount < v.quantity")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now);

    /**
     * Đếm số lượng voucher còn hạn sử dụng
     */
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.status = 'ACTIVE' " +
           "AND v.startDate <= :now " +
           "AND v.endDate >= :now " +
           "AND v.usedCount < v.quantity")
    long countAvailableVouchers(@Param("now") LocalDateTime now);

    /**
     * Tìm các voucher còn hạn, còn số lượng, active và thỏa mãn giá trị đơn hàng tối thiểu
     */
    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' " +
           "AND v.startDate <= :now " +
           "AND v.endDate >= :now " +
           "AND v.usedCount < v.quantity " +
           "AND (v.minOrderAmount IS NULL OR v.minOrderAmount <= :orderAmount)")
    List<Voucher> findValidVouchersForOrder(@Param("now") LocalDateTime now, @Param("orderAmount") BigDecimal orderAmount);
}
