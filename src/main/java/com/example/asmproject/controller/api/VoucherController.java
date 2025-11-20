package com.example.asmproject.controller.api;

import com.example.asmproject.model.Voucher;
import com.example.asmproject.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các API liên quan đến mã giảm giá (voucher)
 * Bao gồm: lấy danh sách voucher có sẵn, lấy voucher theo mã code
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    /**
     * API lấy danh sách các mã giảm giá đang có sẵn
     * 
     * Voucher được coi là "có sẵn" khi:
     * - Status = ACTIVE (đang hoạt động)
     * - Hiện tại nằm trong khoảng thời gian có hiệu lực (startDate <= now <= endDate)
     * - Số lượng đã sử dụng < tổng số lượng (usedCount < quantity)
     * 
     * Dùng để:
     * - Hiển thị danh sách voucher trên trang checkout
     * - Hiển thị voucher promotion trên trang chủ
     * 
     * @return Danh sách các voucher đang có sẵn
     */
    @GetMapping("/available")
    public ResponseEntity<List<Voucher>> getAvailableVouchers() {
        // Gọi service để lấy danh sách voucher có sẵn
        // Service sẽ query từ database với các điều kiện:
        // - status = ACTIVE
        // - startDate <= now <= endDate
        // - usedCount < quantity
        List<Voucher> vouchers = voucherService.getAvailableVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    /**
     * API lấy thông tin voucher theo mã code
     * 
     * Người dùng nhập mã voucher code vào form, API sẽ kiểm tra:
     * - Voucher có tồn tại không
     * - Voucher có đang hoạt động không (status = ACTIVE)
     * - Voucher có còn hiệu lực không (trong khoảng startDate và endDate)
     * - Voucher có còn số lượng không (usedCount < quantity)
     * 
     * Dùng khi:
     * - User nhập mã voucher code trong form checkout
     * - Hệ thống cần validate voucher trước khi áp dụng giảm giá
     * 
     * @param code Mã voucher code (ví dụ: "SALE50", "NEWUSER10")
     * @return JSON response với thông tin voucher nếu hợp lệ, lỗi nếu không hợp lệ
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getVoucherByCode(@PathVariable String code) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate code không được để trống
        if (code == null || code.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã voucher không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Gọi service để tìm voucher theo code
        // Service sẽ tìm voucher có:
        // - code = input code
        // - status = ACTIVE
        // - Còn hiệu lực (trong khoảng thời gian startDate và endDate)
        // - Còn số lượng (usedCount < quantity)
        Optional<Voucher> voucher = voucherService.getActiveVoucherByCode(code);
        
        if (voucher.isPresent()) {
            // Voucher hợp lệ -> trả về thông tin voucher
            Voucher voucherData = voucher.get();
            response.put("success", true);
            response.put("message", "Mã voucher hợp lệ.");
            response.put("voucher", voucherData);
            
            // Thêm thông tin chi tiết về voucher để client dễ hiển thị
            Map<String, Object> voucherInfo = new HashMap<>();
            voucherInfo.put("id", voucherData.getId());
            voucherInfo.put("code", voucherData.getCode());
            voucherInfo.put("description", voucherData.getDescription());
            voucherInfo.put("discountType", voucherData.getDiscountType()); // PERCENTAGE hoặc FIXED
            voucherInfo.put("discountValue", voucherData.getDiscountValue());
            voucherInfo.put("minOrderAmount", voucherData.getMinOrderAmount()); // Đơn hàng tối thiểu
            voucherInfo.put("maxDiscountAmount", voucherData.getMaxDiscountAmount()); // Giảm giá tối đa (nếu là PERCENTAGE)
            voucherInfo.put("startDate", voucherData.getStartDate());
            voucherInfo.put("endDate", voucherData.getEndDate());
            
            response.put("voucherInfo", voucherInfo);
            
            return ResponseEntity.ok(response);
        } else {
            // Voucher không hợp lệ hoặc đã hết hạn
            response.put("success", false);
            response.put("message", "Mã voucher không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API lấy thông tin voucher theo mã code với validation đơn hàng
     * Kiểm tra voucher có áp dụng được cho đơn hàng hiện tại không
     * 
     * @param code Mã voucher code
     * @param orderAmount Tổng giá trị đơn hàng (để kiểm tra minOrderAmount)
     * @return JSON response với thông tin voucher và số tiền được giảm
     */
    @GetMapping("/code/{code}/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(@PathVariable String code,
                                                               @RequestParam(required = false) Double orderAmount) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate code
        if (code == null || code.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã voucher không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Tìm voucher
        Optional<Voucher> voucherOpt = voucherService.getActiveVoucherByCode(code);
        
        if (!voucherOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Mã voucher không hợp lệ hoặc đã hết hạn.");
            return ResponseEntity.badRequest().body(response);
        }
        
        Voucher voucher = voucherOpt.get();
        
        // Kiểm tra đơn hàng tối thiểu nếu có
        if (orderAmount != null && voucher.getMinOrderAmount() != null) {
            if (orderAmount < voucher.getMinOrderAmount().doubleValue()) {
                response.put("success", false);
                response.put("message", "Đơn hàng tối thiểu để sử dụng voucher này là " + voucher.getMinOrderAmount() + " VNĐ.");
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        // Tính số tiền được giảm nếu có orderAmount
        if (orderAmount != null) {
            java.math.BigDecimal orderAmountBD = java.math.BigDecimal.valueOf(orderAmount);
            java.math.BigDecimal discountAmount = voucher.calculateDiscount(orderAmountBD);
            
            response.put("success", true);
            response.put("message", "Mã voucher hợp lệ.");
            response.put("voucher", voucher);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", orderAmountBD.subtract(discountAmount));
        } else {
            response.put("success", true);
            response.put("message", "Mã voucher hợp lệ.");
            response.put("voucher", voucher);
        }
        
        return ResponseEntity.ok(response);
    }
}

