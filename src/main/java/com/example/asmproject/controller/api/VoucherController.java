package com.example.asmproject.controller.api;

import com.example.asmproject.model.Voucher;
import com.example.asmproject.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    /**
     * API lấy danh sách tất cả các voucher có thể sử dụng được
     */
    @GetMapping("/available")
    public ResponseEntity<List<Voucher>> getAvailableVouchers() {
        List<Voucher> vouchers = voucherService.getAvailableVouchers();
        return ResponseEntity.ok(vouchers);
    }

    /**
     * API tìm voucher theo mã code
     * Trả về thông tin voucher và trạng thái hợp lệ (boolean)
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getVoucherByCode(@PathVariable String code) {
        Map<String, Object> response = new HashMap<>();
        Optional<Voucher> voucherOpt = voucherService.getVoucherByCode(code);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            boolean isValid = voucher.isValid();
            
            response.put("valid", isValid);
            response.put("voucher", voucher);
            if (!isValid) {
                response.put("message", "Mã giảm giá đã hết hạn hoặc hết lượt sử dụng");
            }
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại");
            return ResponseEntity.ok(response); // Trả về 200 OK nhưng valid=false
        }
    }
    
    /**
     * API kiểm tra voucher có hợp lệ với đơn hàng không
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkVoucher(@RequestParam String code, 
                                                            @RequestParam BigDecimal amount) {
        Map<String, Object> response = new HashMap<>();
        Optional<Voucher> voucherOpt = voucherService.getVoucherByCode(code);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            if (voucher.isValid()) {
                if (voucher.getMinOrderAmount() == null || amount.compareTo(voucher.getMinOrderAmount()) >= 0) {
                    BigDecimal discount = voucher.calculateDiscount(amount);
                    response.put("valid", true);
                    response.put("voucher", voucher);
                    response.put("discount", discount);
                } else {
                    response.put("valid", false);
                    response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu: " + voucher.getMinOrderAmount());
                }
            } else {
                response.put("valid", false);
                response.put("message", "Mã giảm giá đã hết hạn hoặc hết lượt dùng");
            }
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại");
        }
        return ResponseEntity.ok(response);
    }
}
