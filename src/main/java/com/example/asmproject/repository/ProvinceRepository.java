package com.example.asmproject.repository;

import com.example.asmproject.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Optional<Province> findByCodeTMS(String codeTMS);
    Optional<Province> findByName(String name);
}

