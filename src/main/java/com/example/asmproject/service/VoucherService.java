package com.example.asmproject.service;

import com.example.asmproject.model.Voucher;
import com.example.asmproject.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    public Page<Voucher> getVouchersByStatus(Voucher.VoucherStatus status, Pageable pageable) {
        if (status == null) {
            return voucherRepository.findAll(pageable);
        }
        return voucherRepository.findByStatus(status, pageable);
    }

    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    /**
     * Lấy số lượng voucher đang active
     */
    public long getActiveVoucherCount() {
        return voucherRepository.countAvailableVouchers(LocalDateTime.now());
    }

    /**
     * Lấy danh sách tất cả các voucher có thể sử dụng được (chưa hết hạn, còn lượt
     * dùng)
     * Để hiển thị cho người dùng tham khảo
     */
    public List<Voucher> getAvailableVouchers() {
        return voucherRepository.findAvailableVouchers(LocalDateTime.now());
    }

    /**
     * Lấy danh sách các voucher hợp lệ cho một giá trị đơn hàng cụ thể
     */
    public List<Voucher> getValidVouchersForOrder(BigDecimal orderAmount) {
        return voucherRepository.findValidVouchersForOrder(LocalDateTime.now(), orderAmount);
    }

    /**
     * Kiểm tra tính hợp lệ của một voucher cụ thể
     */
    public boolean isValidVoucher(String code, BigDecimal orderAmount) {
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            return voucher.isValid() &&
                    (voucher.getMinOrderAmount() == null ||
                            orderAmount.compareTo(voucher.getMinOrderAmount()) >= 0);
        }
        return false;
    }
}
