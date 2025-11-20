package com.example.asmproject.service;

import com.example.asmproject.model.Brand;
import com.example.asmproject.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BrandService {
    
    @Autowired
    private BrandRepository brandRepository;
    
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
    
    public Optional<Brand> getBrandById(Long id) {
        return brandRepository.findById(id);
    }
    
    public Brand saveBrand(Brand brand) {
        if (brand.getId() == null && brandRepository.existsByName(brand.getName())) {
            throw new RuntimeException("Thương hiệu đã tồn tại");
        }
        return brandRepository.save(brand);
    }
    
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }
}

