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

@Service
@Transactional
public class VoucherService {
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    public List<Voucher> getAvailableVouchers() {
        return voucherRepository.findAvailableVouchers(LocalDateTime.now());
    }
    
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }
    
    public Optional<Voucher> getActiveVoucherByCode(String code) {
        return voucherRepository.findActiveByCode(code);
    }
    
    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }
    
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
    
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    
    public Page<Voucher> getVouchersByStatus(Voucher.VoucherStatus status, Pageable pageable) {
        return voucherRepository.findByStatus(status, pageable);
    }
    
    public long getActiveVoucherCount() {
        return voucherRepository.countActiveVouchers();
    }
}

