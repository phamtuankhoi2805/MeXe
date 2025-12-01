package com.example.asmproject.service;

import com.example.asmproject.model.ChargingStation;
import com.example.asmproject.repository.ChargingStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChargingStationService {
    
    @Autowired
    private ChargingStationRepository chargingStationRepository;
    
    /**
     * Lấy tất cả trạm sạc đang hoạt động
     */
    public List<ChargingStation> getAllActiveStations() {
        return chargingStationRepository.findAllActive();
    }
    
    /**
     * Tìm trạm sạc gần nhất dựa trên tọa độ
     * 
     * @param latitude Vĩ độ của vị trí hiện tại
     * @param longitude Kinh độ của vị trí hiện tại
     * @param limit Số lượng trạm cần lấy (mặc định 10)
     * @return Danh sách trạm sạc gần nhất, sắp xếp theo khoảng cách
     */
    public List<ChargingStation> findNearestStations(BigDecimal latitude, BigDecimal longitude, int limit) {
        List<ChargingStation> allStations = chargingStationRepository.findAllActive();
        
        // Tính khoảng cách và sắp xếp
        return allStations.stream()
                .map(station -> {
                    // Tính khoảng cách và lưu tạm (sử dụng một cách đơn giản)
                    double distance = station.calculateDistance(latitude, longitude);
                    return new StationWithDistance(station, distance);
                })
                .sorted(Comparator.comparingDouble(StationWithDistance::getDistance))
                .limit(limit)
                .map(StationWithDistance::getStation)
                .collect(Collectors.toList());
    }
    
    /**
     * Tìm trạm sạc gần nhất (mặc định 10 trạm)
     */
    public List<ChargingStation> findNearestStations(BigDecimal latitude, BigDecimal longitude) {
        return findNearestStations(latitude, longitude, 10);
    }
    
    /**
     * Tìm trạm sạc theo tỉnh/thành phố
     */
    public List<ChargingStation> findByProvince(String province) {
        return chargingStationRepository.findByProvince(province);
    }
    
    /**
     * Tìm trạm sạc theo quận/huyện
     */
    public List<ChargingStation> findByDistrict(String district) {
        return chargingStationRepository.findByDistrict(district);
    }
    
    /**
     * Lưu trạm sạc
     */
    public ChargingStation saveStation(ChargingStation station) {
        return chargingStationRepository.save(station);
    }
    
    /**
     * Lấy trạm sạc theo ID
     */
    public ChargingStation getStationById(Long id) {
        return chargingStationRepository.findById(id).orElse(null);
    }
    
    /**
     * Helper class để lưu trạm và khoảng cách
     */
    private static class StationWithDistance {
        private final ChargingStation station;
        private final double distance;
        
        public StationWithDistance(ChargingStation station, double distance) {
            this.station = station;
            this.distance = distance;
        }
        
        public ChargingStation getStation() {
            return station;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}

