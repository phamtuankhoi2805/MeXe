package com.example.asmproject.controller.api;

import com.example.asmproject.model.Address;
import com.example.asmproject.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        List<Address> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId"));
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            String province = request.get("province");
            String district = request.get("district");
            String ward = request.get("ward");
            String street = request.get("street");
            
            Address address = addressService.addAddress(userId, fullName, phone, 
                                                       province, district, ward, street);
            response.put("success", true);
            response.put("message", "Thêm địa chỉ thành công.");
            response.put("address", address);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(@PathVariable Long id,
                                                             @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            String province = request.get("province");
            String district = request.get("district");
            String ward = request.get("ward");
            String street = request.get("street");
            
            Address address = addressService.updateAddress(id, fullName, phone, 
                                                          province, district, ward, street);
            response.put("success", true);
            response.put("message", "Cập nhật địa chỉ thành công.");
            response.put("address", address);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Long id,
                                                                  @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            addressService.setDefaultAddress(userId, id);
            response.put("success", true);
            response.put("message", "Đã đặt làm địa chỉ mặc định.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Long id,
                                                             @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            addressService.deleteAddress(userId, id);
            response.put("success", true);
            response.put("message", "Đã xóa địa chỉ.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/default/{userId}")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long userId) {
        Optional<Address> address = addressService.getDefaultAddress(userId);
        return address.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
}

