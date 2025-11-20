package com.example.asmproject.service;

import com.example.asmproject.model.Voucher;
import com.example.asmproject.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý các logic nghiệp vụ liên quan đến mã giảm giá (voucher)
 * Bao gồm: lấy danh sách voucher có sẵn, lấy voucher theo code, validate voucher
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Service
@Transactional
public class VoucherService {
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    /**
     * Lấy danh sách các voucher đang có sẵn
     * 
     * Voucher được coi là "có sẵn" khi:
     * - status = ACTIVE (đang hoạt động)
     * - Hiện tại nằm trong khoảng thời gian có hiệu lực (startDate <= now <= endDate)
     * - Số lượng đã sử dụng < tổng số lượng (usedCount < quantity)
     * 
     * @return Danh sách các voucher đang có sẵn
     */
    public List<Voucher> getAvailableVouchers() {
        // Query từ database với các điều kiện:
        // - status = ACTIVE
        // - startDate <= now <= endDate
        // - usedCount < quantity
        return voucherRepository.findAvailableVouchers(LocalDateTime.now());
    }
    
    /**
     * Lấy voucher theo code (không kiểm tra trạng thái)
     * 
     * @param code Mã voucher code
     * @return Optional<Voucher> - có thể empty nếu không tìm thấy
     */
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }
    
    /**
     * Lấy voucher đang hoạt động theo code
     * 
     * Voucher được coi là "đang hoạt động" khi:
     * - status = ACTIVE
     * - Còn hiệu lực (trong khoảng startDate và endDate)
     * - Còn số lượng (usedCount < quantity)
     * 
     * @param code Mã voucher code
     * @return Optional<Voucher> - có thể empty nếu không tìm thấy hoặc không hợp lệ
     */
    public Optional<Voucher> getActiveVoucherByCode(String code) {
        // Query từ database với các điều kiện:
        // - code = input code
        // - status = ACTIVE
        // - Còn hiệu lực (trong khoảng thời gian startDate và endDate)
        // - Còn số lượng (usedCount < quantity)
        return voucherRepository.findActiveByCode(code);
    }
    
    /**
     * Lưu voucher (thêm mới hoặc cập nhật)
     * 
     * @param voucher Voucher object cần lưu
     * @return Voucher object đã được lưu vào database
     */
    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }
    
    /**
     * Xóa voucher theo ID
     * 
     * @param id ID của voucher cần xóa
     */
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
    
    /**
     * Lấy tất cả voucher (không filter)
     * 
     * @return Danh sách tất cả voucher trong database
     */
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    
    /**
     * Lấy danh sách voucher theo trạng thái với phân trang
     * 
     * @param status Trạng thái voucher (ACTIVE, INACTIVE, EXPIRED)
     * @param pageable Thông tin phân trang (page, size, sort)
     * @return Page<Voucher> - Danh sách voucher đã được phân trang
     */
    public Page<Voucher> getVouchersByStatus(Voucher.VoucherStatus status, Pageable pageable) {
        return voucherRepository.findByStatus(status, pageable);
    }
    
    /**
     * Đếm tổng số voucher đang hoạt động (status = ACTIVE)
     * 
     * @return Tổng số voucher đang hoạt động
     */
    public long getActiveVoucherCount() {
        return voucherRepository.countActiveVouchers();
    }
}

