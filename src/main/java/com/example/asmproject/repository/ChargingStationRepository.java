package com.example.asmproject.repository;

import com.example.asmproject.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    
    List<ChargingStation> findByStatus(ChargingStation.StationStatus status);
    
    @Query("SELECT cs FROM ChargingStation cs WHERE cs.status = 'ACTIVE'")
    List<ChargingStation> findAllActive();
    
    @Query("SELECT cs FROM ChargingStation cs " +
           "WHERE cs.status = 'ACTIVE' " +
           "AND cs.province = :province")
    List<ChargingStation> findByProvince(@Param("province") String province);
    
    @Query("SELECT cs FROM ChargingStation cs " +
           "WHERE cs.status = 'ACTIVE' " +
           "AND cs.district = :district")
    List<ChargingStation> findByDistrict(@Param("district") String district);
}

