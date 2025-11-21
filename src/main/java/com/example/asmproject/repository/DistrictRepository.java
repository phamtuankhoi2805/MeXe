package com.example.asmproject.repository;

import com.example.asmproject.model.District;
import com.example.asmproject.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByProvince(Province province);
    List<District> findByProvinceId(Long provinceId);
    Optional<District> findByCode(String code);
}

