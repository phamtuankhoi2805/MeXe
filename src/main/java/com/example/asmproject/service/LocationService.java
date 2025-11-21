package com.example.asmproject.service;

import com.example.asmproject.model.District;
import com.example.asmproject.model.Province;
import com.example.asmproject.model.Ward;
import com.example.asmproject.repository.DistrictRepository;
import com.example.asmproject.repository.ProvinceRepository;
import com.example.asmproject.repository.WardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class LocationService {
    
    @Autowired
    private ProvinceRepository provinceRepository;
    
    @Autowired
    private DistrictRepository districtRepository;
    
    @Autowired
    private WardRepository wardRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Đọc dữ liệu từ JSON file (không cache, đọc mỗi lần)
     */
    private JsonNode readJsonData() {
        try {
            ClassPathResource resource = new ClassPathResource("data/danhmucxaphuong.json");
            InputStream inputStream = resource.getInputStream();
            return objectMapper.readTree(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy danh sách tất cả tỉnh/thành phố từ JSON
     */
    public List<Map<String, Object>> getAllProvincesFromJson() {
        JsonNode rootNode = readJsonData();
        List<Map<String, Object>> provinces = new ArrayList<>();
        
        for (JsonNode provinceNode : rootNode) {
            Map<String, Object> province = new HashMap<>();
            province.put("codeBNV", provinceNode.get("matinhBNV").asText());
            province.put("codeTMS", provinceNode.get("matinhTMS").asText());
            province.put("name", provinceNode.get("tentinhmoi").asText());
            provinces.add(province);
        }
        
        return provinces;
    }
    
    /**
     * Lấy danh sách quận/huyện theo tỉnh từ JSON
     * Parse từ mã phường/xã để tách ra quận/huyện và lấy tên từ tên phường/xã
     */
    public List<Map<String, Object>> getDistrictsByProvinceFromJson(String provinceCodeTMS) {
        JsonNode rootNode = readJsonData();
        Map<String, Map<String, Object>> districtMap = new LinkedHashMap<>();
        Map<String, List<JsonNode>> districtWardsMap = new HashMap<>();
        
        // Tìm tỉnh
        for (JsonNode provinceNode : rootNode) {
            if (provinceNode.get("matinhTMS").asText().equals(provinceCodeTMS)) {
                JsonNode wardsNode = provinceNode.get("phuongxa");
                if (wardsNode != null && wardsNode.isArray()) {
                    // Bước 1: Nhóm phường/xã theo mã quận/huyện
                    for (JsonNode wardNode : wardsNode) {
                        Long wardCode = wardNode.get("maphuongxa").asLong();
                        String wardCodeStr = String.valueOf(wardCode);
                        
                        // Parse mã: [3 số tỉnh][2 số quận/huyện][3 số phường/xã]
                        if (wardCodeStr.length() == 8 && wardCodeStr.startsWith(provinceCodeTMS)) {
                            String districtCode = wardCodeStr.substring(3, 5);
                            String fullDistrictCode = provinceCodeTMS + districtCode;
                            
                            // Nhóm phường/xã theo quận/huyện
                            districtWardsMap.computeIfAbsent(fullDistrictCode, k -> new ArrayList<>()).add(wardNode);
                        }
                    }
                    
                    // Bước 2: Tạo quận/huyện từ nhóm phường/xã
                    for (Map.Entry<String, List<JsonNode>> entry : districtWardsMap.entrySet()) {
                        String fullDistrictCode = entry.getKey();
                        List<JsonNode> wards = entry.getValue();
                        
                        // Lấy tên quận/huyện từ phường/xã đầu tiên
                        String districtName = extractDistrictName(wards);
                        
                        Map<String, Object> district = new HashMap<>();
                        district.put("code", fullDistrictCode);
                        district.put("name", districtName);
                        district.put("provinceCode", provinceCodeTMS);
                        districtMap.put(fullDistrictCode, district);
                    }
                }
                break;
            }
        }
        
        return new ArrayList<>(districtMap.values());
    }
    
    /**
     * Extract tên quận/huyện từ danh sách phường/xã
     * Logic: 
     * - Lấy tên từ phường/xã đầu tiên trong nhóm
     * - Nếu bắt đầu bằng "Phường" thì là "Quận", nếu "Xã" hoặc "Thị trấn" thì là "Huyện"
     * - Ví dụ: "Phường Cầu Giấy" -> "Quận Cầu Giấy", "Xã Thanh Trì" -> "Huyện Thanh Trì"
     */
    private String extractDistrictName(List<JsonNode> wards) {
        if (wards == null || wards.isEmpty()) {
            return "Quận/Huyện";
        }
        
        // Lấy tên phường/xã đầu tiên
        String firstWardName = wards.get(0).get("tenphuongxa").asText();
        String districtName;
        String districtType;
        
        if (firstWardName.startsWith("Phường")) {
            // Bỏ "Phường " ở đầu (7 ký tự) và lấy phần còn lại làm tên quận
            String name = firstWardName.substring(7).trim();
            districtName = name;
            districtType = "Quận";
        } else if (firstWardName.startsWith("Xã")) {
            // Bỏ "Xã " ở đầu (3 ký tự) và lấy phần còn lại làm tên huyện
            String name = firstWardName.substring(3).trim();
            districtName = name;
            districtType = "Huyện";
        } else if (firstWardName.startsWith("Thị trấn")) {
            // Bỏ "Thị trấn " ở đầu (9 ký tự)
            String name = firstWardName.substring(9).trim();
            districtName = name;
            districtType = "Huyện";
        } else {
            // Mặc định
            districtName = "Quận/Huyện";
            districtType = "";
        }
        
        return districtType.isEmpty() ? districtName : districtType + " " + districtName;
    }
    
    /**
     * Lấy danh sách phường/xã theo quận/huyện từ JSON
     */
    public List<Map<String, Object>> getWardsByDistrictFromJson(String provinceCodeTMS, String districtCode) {
        JsonNode rootNode = readJsonData();
        List<Map<String, Object>> wards = new ArrayList<>();
        
        // Tìm tỉnh
        for (JsonNode provinceNode : rootNode) {
            if (provinceNode.get("matinhTMS").asText().equals(provinceCodeTMS)) {
                JsonNode wardsNode = provinceNode.get("phuongxa");
                if (wardsNode != null && wardsNode.isArray()) {
                    for (JsonNode wardNode : wardsNode) {
                        Long wardCode = wardNode.get("maphuongxa").asLong();
                        String wardCodeStr = String.valueOf(wardCode);
                        
                        // Kiểm tra xem phường/xã này thuộc quận/huyện nào
                        if (wardCodeStr.length() == 8 && wardCodeStr.startsWith(provinceCodeTMS)) {
                            String wardDistrictCode = wardCodeStr.substring(3, 5);
                            String fullDistrictCode = provinceCodeTMS + wardDistrictCode;
                            
                            if (fullDistrictCode.equals(districtCode)) {
                                Map<String, Object> ward = new HashMap<>();
                                ward.put("code", wardCode);
                                ward.put("name", wardNode.get("tenphuongxa").asText());
                                ward.put("districtCode", fullDistrictCode);
                                wards.add(ward);
                            }
                        }
                    }
                }
                break;
            }
        }
        
        return wards;
    }
    
    /**
     * Lấy danh sách tất cả tỉnh/thành phố từ database
     */
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }
    
    /**
     * Lấy danh sách quận/huyện theo tỉnh từ database
     */
    public List<District> getDistrictsByProvince(Long provinceId) {
        return districtRepository.findByProvinceId(provinceId);
    }
    
    /**
     * Lấy danh sách phường/xã theo quận/huyện từ database
     */
    public List<Ward> getWardsByDistrict(Long districtId) {
        return wardRepository.findByDistrictId(districtId);
    }
    
    /**
     * Tìm tỉnh theo tên
     */
    public Optional<Province> findProvinceByName(String name) {
        return provinceRepository.findByName(name);
    }
    
    /**
     * Tìm quận/huyện theo mã
     */
    public Optional<District> findDistrictByCode(String code) {
        return districtRepository.findByCode(code);
    }
    
    /**
     * Tìm phường/xã theo mã
     */
    public Optional<Ward> findWardByCode(Long code) {
        return wardRepository.findByCode(code);
    }
    
    /**
     * Import dữ liệu từ JSON file vào database
     * Parse mã phường/xã để tách ra quận/huyện
     * Format mã: [mã tỉnh 3 số][mã quận/huyện 2 số][mã phường/xã 3 số]
     */
    public void importFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/danhmucxaphuong.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            
            Map<String, Province> provinceMap = new HashMap<>();
            Map<String, District> districtMap = new HashMap<>();
            
            // Duyệt qua từng tỉnh
            for (JsonNode provinceNode : rootNode) {
                String codeTMS = provinceNode.get("matinhTMS").asText();
                String codeBNV = provinceNode.get("matinhBNV").asText();
                String provinceName = provinceNode.get("tentinhmoi").asText();
                
                // Tạo hoặc lấy tỉnh
                Province province = provinceMap.get(codeTMS);
                if (province == null) {
                    Optional<Province> existing = provinceRepository.findByCodeTMS(codeTMS);
                    if (existing.isPresent()) {
                        province = existing.get();
                    } else {
                        province = new Province(codeBNV, codeTMS, provinceName);
                        province = provinceRepository.save(province);
                    }
                    provinceMap.put(codeTMS, province);
                }
                
                // Duyệt qua từng phường/xã để tách ra quận/huyện
                JsonNode wardsNode = provinceNode.get("phuongxa");
                if (wardsNode != null && wardsNode.isArray()) {
                    Map<String, List<JsonNode>> districtWardsMap = new HashMap<>();
                    
                    for (JsonNode wardNode : wardsNode) {
                        Long wardCode = wardNode.get("maphuongxa").asLong();
                        
                        // Parse mã phường/xã để lấy mã quận/huyện
                        // Format: [mã tỉnh 3 số][mã quận/huyện 2 số][mã phường/xã 3 số] = 8 số
                        String wardCodeStr = String.valueOf(wardCode);
                        if (wardCodeStr.length() == 8) {
                            // Lấy 2 số giữa làm mã quận/huyện (sau 3 số đầu, trước 3 số cuối)
                            String districtCode = wardCodeStr.substring(3, 5);
                            String fullDistrictCode = codeTMS + districtCode;
                            
                            // Nhóm phường/xã theo quận/huyện
                            districtWardsMap.computeIfAbsent(fullDistrictCode, k -> new ArrayList<>()).add(wardNode);
                        }
                    }
                    
                        // Tạo quận/huyện và phường/xã
                        for (Map.Entry<String, List<JsonNode>> entry : districtWardsMap.entrySet()) {
                            String districtCode = entry.getKey();
                            List<JsonNode> wards = entry.getValue();
                            
                            // Lấy tên quận/huyện từ mã
                            // Có thể cải thiện bằng cách parse từ tên phường/xã sau
                            String districtName = "Quận/Huyện " + districtCode.substring(3);
                            
                            // Tạo hoặc lấy quận/huyện
                            District district = districtMap.get(districtCode);
                            if (district == null) {
                                Optional<District> existing = districtRepository.findByCode(districtCode);
                                if (existing.isPresent()) {
                                    district = existing.get();
                                } else {
                                    district = new District(province, districtCode, districtName);
                                    district = districtRepository.save(district);
                                }
                                districtMap.put(districtCode, district);
                            }
                            
                            // Tạo phường/xã
                            for (JsonNode wardNode : wards) {
                                Long wardCode = wardNode.get("maphuongxa").asLong();
                                String wardName = wardNode.get("tenphuongxa").asText();
                                
                                Optional<Ward> existingWard = wardRepository.findByCode(wardCode);
                                if (!existingWard.isPresent()) {
                                    Ward ward = new Ward(district, wardCode, wardName);
                                    wardRepository.save(ward);
                                }
                            }
                        }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi import dữ liệu địa chỉ từ JSON: " + e.getMessage(), e);
        }
    }
    
}

