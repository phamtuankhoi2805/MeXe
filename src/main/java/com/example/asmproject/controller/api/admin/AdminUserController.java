package com.example.asmproject.controller.api.admin;

import com.example.asmproject.dto.UserRequest;
import com.example.asmproject.dto.UserResponse;
import com.example.asmproject.model.enums.UserStatus;
import com.example.asmproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý người dùng (Admin).
 * Cho phép admin xem danh sách, tạo mới, sửa và khóa tài khoản người dùng.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * Tìm kiếm người dùng theo từ khóa và trạng thái.
     * Kết quả trả về có phân trang.
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> users = userService.searchUsers(keyword, status, PageRequest.of(page, size));
        return ResponseEntity.ok(users);
    }

    /**
     * Lấy thông tin chi tiết của một người dùng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable @NonNull Long id) {
        return userService.findById(id)
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setFullName(user.getFullName());
                    response.setEmail(user.getEmail());
                    response.setPhone(user.getPhone());
                    // Lấy địa chỉ đầu tiên làm đại diện nếu có
                    if (!user.getAddresses().isEmpty()) {
                        response.setAddress(user.getAddresses().get(0).toString());
                    }
                    response.setStatus(user.getEnabled() ? UserStatus.HOAT_DONG : UserStatus.TAM_KHOA);
                    response.setCreatedAt(user.getCreatedAt());
                    response.setUpdatedAt(user.getUpdatedAt());
                    return response;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo mới tài khoản người dùng từ trang admin.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserResponse created = userService.create(request);
            response.put("success", true);
            response.put("message", "Tạo người dùng thành công.");
            response.put("user", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật thông tin người dùng.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable @NonNull Long id,
            @Valid @RequestBody UserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserResponse updated = userService.update(id, request);
            response.put("success", true);
            response.put("message", "Cập nhật người dùng thành công.");
            response.put("user", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa (khóa) tài khoản người dùng.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable @NonNull Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deactivate(id);
            response.put("success", true);
            response.put("message", "Xóa người dùng thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
