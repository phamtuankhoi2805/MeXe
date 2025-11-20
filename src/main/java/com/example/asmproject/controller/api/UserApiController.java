package com.example.asmproject.controller.api;

import com.example.asmproject.dto.OrderResponse;
import com.example.asmproject.dto.UserRequest;
import com.example.asmproject.dto.UserResponse;
import com.example.asmproject.model.enums.UserStatus;
import com.example.asmproject.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserResponse> timKiemNguoiDung(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) UserStatus status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return userService.searchUsers(keyword, status, PageRequest.of(page, size));
    }

    @PostMapping
    public UserResponse taoNguoiDung(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse capNhatNguoiDung(@PathVariable @NonNull Long id,
                                         @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void huyKichHoatNguoiDung(@PathVariable @NonNull Long id) {
        userService.deactivate(id);
    }

    @GetMapping("/{id}/orders")
    public List<OrderResponse> lichSuMuaHang(@PathVariable @NonNull Long id) {
        return userService.getPurchaseHistory(id);
    }
}

