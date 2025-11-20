package com.example.asmproject.repository;

import com.example.asmproject.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.status = 'ACTIVE'")
    Optional<Voucher> findActiveByCode(@Param("code") String code);
    
    List<Voucher> findByStatus(Voucher.VoucherStatus status);
    
    Page<Voucher> findByStatus(Voucher.VoucherStatus status, Pageable pageable);
    
    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' AND " +
           "v.startDate <= :now AND v.endDate >= :now AND v.usedCount < v.quantity")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.status = 'ACTIVE'")
    long countActiveVouchers();
}

