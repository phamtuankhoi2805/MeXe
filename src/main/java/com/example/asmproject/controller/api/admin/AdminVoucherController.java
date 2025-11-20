package com.example.asmproject.controller.api.admin;

import com.example.asmproject.model.Voucher;
import com.example.asmproject.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vouchers")
public class AdminVoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @GetMapping
    public ResponseEntity<Page<Voucher>> getAllVouchers(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Voucher.VoucherStatus voucherStatus = null;
        if (status != null) {
            try {
                voucherStatus = Voucher.VoucherStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {}
        }
        
        Page<Voucher> vouchers = voucherService.getVouchersByStatus(voucherStatus, pageable);
        return ResponseEntity.ok(vouchers);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createVoucher(@RequestBody Voucher voucher) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher saved = voucherService.saveVoucher(voucher);
            response.put("success", true);
            response.put("message", "Tạo voucher thành công.");
            response.put("voucher", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVoucher(@PathVariable Long id,
                                                             @RequestBody Voucher voucher) {
        Map<String, Object> response = new HashMap<>();
        try {
            voucher.setId(id);
            Voucher updated = voucherService.saveVoucher(voucher);
            response.put("success", true);
            response.put("message", "Cập nhật voucher thành công.");
            response.put("voucher", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVoucher(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            voucherService.deleteVoucher(id);
            response.put("success", true);
            response.put("message", "Xóa voucher thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

