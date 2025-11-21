package com.example.asmproject.controller.api;

import com.example.asmproject.model.District;
import com.example.asmproject.model.Province;
import com.example.asmproject.model.Ward;
import com.example.asmproject.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    @Autowired
    private LocationService locationService;
    
    /**
     * API lấy danh sách tất cả tỉnh/thành phố từ JSON
     * GET /api/locations/provinces
     */
    @GetMapping("/provinces")
    public ResponseEntity<List<Map<String, Object>>> getAllProvinces() {
        List<Map<String, Object>> provinces = locationService.getAllProvincesFromJson();
        return ResponseEntity.ok(provinces);
    }
    
    /**
     * API lấy danh sách quận/huyện theo tỉnh từ JSON
     * GET /api/locations/provinces/{provinceCodeTMS}/districts
     * Ví dụ: /api/locations/provinces/101/districts
     */
    @GetMapping("/provinces/{provinceCodeTMS}/districts")
    public ResponseEntity<List<Map<String, Object>>> getDistrictsByProvince(@PathVariable String provinceCodeTMS) {
        List<Map<String, Object>> districts = locationService.getDistrictsByProvinceFromJson(provinceCodeTMS);
        return ResponseEntity.ok(districts);
    }
    
    /**
     * API lấy danh sách phường/xã theo quận/huyện từ JSON
     * GET /api/locations/districts/{districtCode}/wards
     * Ví dụ: /api/locations/districts/10105/wards
     */
    @GetMapping("/districts/{districtCode}/wards")
    public ResponseEntity<List<Map<String, Object>>> getWardsByDistrict(@PathVariable String districtCode) {
        // districtCode format: [mã tỉnh 3 số][mã quận/huyện 2 số] = 5 số
        if (districtCode.length() < 5) {
            return ResponseEntity.badRequest().build();
        }
        String provinceCodeTMS = districtCode.substring(0, 3);
        List<Map<String, Object>> wards = locationService.getWardsByDistrictFromJson(provinceCodeTMS, districtCode);
        return ResponseEntity.ok(wards);
    }
    
    /**
     * API lấy danh sách quận/huyện theo tỉnh từ database (nếu đã import)
     * GET /api/locations/provinces/{provinceId}/districts-db
     */
    @GetMapping("/provinces/{provinceId}/districts-db")
    public ResponseEntity<List<District>> getDistrictsByProvinceFromDb(@PathVariable Long provinceId) {
        List<District> districts = locationService.getDistrictsByProvince(provinceId);
        return ResponseEntity.ok(districts);
    }
    
    /**
     * API lấy danh sách phường/xã theo quận/huyện từ database (nếu đã import)
     * GET /api/locations/districts/{districtId}/wards-db
     */
    @GetMapping("/districts/{districtId}/wards-db")
    public ResponseEntity<List<Ward>> getWardsByDistrictFromDb(@PathVariable Long districtId) {
        List<Ward> wards = locationService.getWardsByDistrict(districtId);
        return ResponseEntity.ok(wards);
    }
    
    /**
     * API tìm kiếm tỉnh theo tên
     * GET /api/locations/provinces/search?name=Hà Nội
     */
    @GetMapping("/provinces/search")
    public ResponseEntity<Province> searchProvince(@RequestParam String name) {
        Optional<Province> province = locationService.findProvinceByName(name);
        return province.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * API tìm kiếm quận/huyện theo mã
     * GET /api/locations/districts/search?code=10105
     */
    @GetMapping("/districts/search")
    public ResponseEntity<District> searchDistrict(@RequestParam String code) {
        Optional<District> district = locationService.findDistrictByCode(code);
        return district.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * API tìm kiếm phường/xã theo mã
     * GET /api/locations/wards/search?code=10105001
     */
    @GetMapping("/wards/search")
    public ResponseEntity<Ward> searchWard(@RequestParam Long code) {
        Optional<Ward> ward = locationService.findWardByCode(code);
        return ward.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * API import dữ liệu từ JSON (chỉ dùng cho admin)
     * POST /api/locations/import
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData() {
        Map<String, Object> response = new HashMap<>();
        try {
            locationService.importFromJson();
            response.put("success", true);
            response.put("message", "Import dữ liệu địa chỉ thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi import: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API lấy cây địa chỉ đầy đủ (tỉnh -> quận/huyện -> phường/xã)
     * GET /api/locations/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getLocationTree() {
        Map<String, Object> tree = new HashMap<>();
        List<Province> provinces = locationService.getAllProvinces();
        
        for (Province province : provinces) {
            Map<String, Object> provinceData = new HashMap<>();
            provinceData.put("id", province.getId());
            provinceData.put("name", province.getName());
            provinceData.put("codeBNV", province.getCodeBNV());
            provinceData.put("codeTMS", province.getCodeTMS());
            
            List<Map<String, Object>> districtsData = new java.util.ArrayList<>();
            List<District> districts = locationService.getDistrictsByProvince(province.getId());
            
            for (District district : districts) {
                Map<String, Object> districtData = new HashMap<>();
                districtData.put("id", district.getId());
                districtData.put("name", district.getName());
                districtData.put("code", district.getCode());
                
                List<Map<String, Object>> wardsData = new java.util.ArrayList<>();
                List<Ward> wards = locationService.getWardsByDistrict(district.getId());
                
                for (Ward ward : wards) {
                    Map<String, Object> wardData = new HashMap<>();
                    wardData.put("id", ward.getId());
                    wardData.put("name", ward.getName());
                    wardData.put("code", ward.getCode());
                    wardsData.add(wardData);
                }
                
                districtData.put("wards", wardsData);
                districtsData.add(districtData);
            }
            
            provinceData.put("districts", districtsData);
            tree.put(province.getName(), provinceData);
        }
        
        return ResponseEntity.ok(tree);
    }
}

