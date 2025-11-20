package com.example.asmproject.controller.api;

import com.example.asmproject.model.Voucher;
import com.example.asmproject.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @GetMapping("/available")
    public ResponseEntity<List<Voucher>> getAvailableVouchers() {
        List<Voucher> vouchers = voucherService.getAvailableVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getVoucherByCode(@PathVariable String code) {
        Map<String, Object> response = new java.util.HashMap<>();
        Optional<Voucher> voucher = voucherService.getActiveVoucherByCode(code);
        if (voucher.isPresent()) {
            response.put("success", true);
            response.put("voucher", voucher.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Mã voucher không hợp lệ hoặc đã hết hạn.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

