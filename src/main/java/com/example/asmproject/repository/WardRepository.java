package com.example.asmproject.repository;

import com.example.asmproject.model.District;
import com.example.asmproject.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByDistrict(District district);
    List<Ward> findByDistrictId(Long districtId);
    Optional<Ward> findByCode(Long code);
}

