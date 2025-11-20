package com.example.asmproject.controller.api;

import com.example.asmproject.model.Address;
import com.example.asmproject.service.AddressService;
import com.example.asmproject.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các API liên quan đến quản lý địa chỉ
 * Bao gồm: thêm, sửa, xóa địa chỉ, đặt địa chỉ mặc định
 * 
 * Giới hạn: Mỗi user chỉ có thể thêm tối đa 4 địa chỉ
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    /**
     * API lấy danh sách địa chỉ của user
     * Sắp xếp theo địa chỉ mặc định trước (default = true), sau đó mới đến các địa chỉ khác
     * 
     * Chỉ user đã đăng nhập mới có thể xem địa chỉ của chính mình
     * 
     * @param userId ID của người dùng
     * @return Danh sách địa chỉ của user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @securityUtil.isOwnerOrAdmin(#userId))")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        // Gọi service để lấy danh sách địa chỉ của user
        // Service sẽ query từ database và sắp xếp theo địa chỉ mặc định trước
        List<Address> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * API thêm địa chỉ mới
     * 
     * Giới hạn: Mỗi user chỉ có thể thêm tối đa 4 địa chỉ
     * - Nếu đã có 4 địa chỉ thì không thể thêm nữa
     * - Nếu đây là địa chỉ đầu tiên thì tự động đặt làm địa chỉ mặc định
     * 
     * Chỉ user đã đăng nhập mới có thể thêm địa chỉ của chính mình
     * 
     * @param request Chứa: userId, fullName, phone, province, district, ward, street
     * @return JSON response với thông báo và thông tin địa chỉ đã thêm
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin từ request
            Long userId = request.get("userId") != null 
                ? Long.valueOf(request.get("userId")) 
                : securityUtil.getCurrentUserId();
            
            // Nếu không có userId thì lấy từ user hiện tại
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Bạn phải đăng nhập để thêm địa chỉ.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra user chỉ có thể thêm địa chỉ của chính mình
            Long currentUserId = securityUtil.getCurrentUserId();
            if (!securityUtil.isAdmin() && !userId.equals(currentUserId)) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền thêm địa chỉ cho người khác.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            String province = request.get("province");
            String district = request.get("district");
            String ward = request.get("ward");
            String street = request.get("street");
            
            // Validate dữ liệu
            if (fullName == null || fullName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Họ tên không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Số điện thoại không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gọi service để thêm địa chỉ
            // Service sẽ:
            // - Kiểm tra số lượng địa chỉ hiện tại (tối đa 4)
            // - Nếu là địa chỉ đầu tiên thì tự động đặt làm mặc định
            // - Lưu vào database
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
    
    /**
     * API cập nhật địa chỉ
     * 
     * @param id ID của địa chỉ cần cập nhật
     * @param request Chứa: fullName, phone, province, district, ward, street
     * @return JSON response với thông báo và thông tin địa chỉ đã cập nhật
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(@PathVariable Long id,
                                                             @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin từ request
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            String province = request.get("province");
            String district = request.get("district");
            String ward = request.get("ward");
            String street = request.get("street");
            
            // Gọi service để cập nhật địa chỉ
            // Service sẽ:
            // - Tìm địa chỉ theo ID
            // - Cập nhật thông tin mới
            // - Lưu vào database
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
    
    /**
     * API đặt địa chỉ làm mặc định
     * Chỉ có 1 địa chỉ mặc định tại một thời điểm
     * Khi đặt địa chỉ mới làm mặc định thì địa chỉ cũ sẽ tự động bỏ mặc định
     * 
     * @param id ID của địa chỉ muốn đặt làm mặc định
     * @param userId ID của người dùng (để xác thực quyền)
     * @return JSON response với thông báo kết quả
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Long id,
                                                                  @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi service để đặt địa chỉ làm mặc định
            // Service sẽ:
            // - Bỏ mặc định tất cả địa chỉ hiện tại của user
            // - Đặt địa chỉ được chọn làm mặc định
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
    
    /**
     * API xóa địa chỉ
     * 
     * Lưu ý: Không thể xóa địa chỉ đang được sử dụng trong đơn hàng
     * Nếu xóa địa chỉ mặc định, hệ thống sẽ tự động đặt một địa chỉ khác làm mặc định
     * 
     * @param id ID của địa chỉ cần xóa
     * @param userId ID của người dùng (để xác thực quyền)
     * @return JSON response với thông báo kết quả
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Long id,
                                                             @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi service để xóa địa chỉ
            // Service sẽ:
            // - Kiểm tra địa chỉ có đang được sử dụng trong đơn hàng không
            // - Nếu có thì không cho xóa
            // - Nếu không thì xóa
            // - Nếu xóa địa chỉ mặc định thì tự động đặt địa chỉ khác làm mặc định
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
    
    /**
     * API lấy địa chỉ mặc định của user
     * 
     * @param userId ID của người dùng
     * @return Address object nếu tìm thấy, 404 nếu không có địa chỉ mặc định
     */
    @GetMapping("/default/{userId}")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long userId) {
        // Gọi service để lấy địa chỉ mặc định
        Optional<Address> address = addressService.getDefaultAddress(userId);
        
        // Nếu tìm thấy thì trả về, nếu không thì trả về 404
        return address.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
}

