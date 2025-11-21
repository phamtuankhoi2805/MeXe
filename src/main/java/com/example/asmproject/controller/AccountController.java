package com.example.asmproject.controller;

import com.example.asmproject.model.Address;
import com.example.asmproject.model.User;
import com.example.asmproject.repository.UserRepository;
import com.example.asmproject.service.AddressService;
import com.example.asmproject.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AccountController {
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Trang thông tin tài khoản
     * Kiểm tra nếu user mới (chưa có phone hoặc address) thì hiển thị form nhập thông tin
     */
    @GetMapping("/tai-khoan")
    public String hienThiTaiKhoan(Model model) {
        Optional<User> userOpt = securityUtil.getCurrentUser();
        
        if (!userOpt.isPresent()) {
            return "redirect:/dang-ky/tai-khoan";
        }
        
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("trangDangChon", "tai-khoan");
        
        // Kiểm tra nếu user mới (chưa có phone hoặc chưa có address)
        boolean isNewUser = (user.getPhone() == null || user.getPhone().isEmpty()) 
                          || addressService.getUserAddresses(user.getId()).isEmpty();
        model.addAttribute("isNewUser", isNewUser);
        
        // Lấy danh sách địa chỉ
        List<Address> addresses = addressService.getUserAddresses(user.getId());
        model.addAttribute("addresses", addresses);
        
        // Kiểm tra xem có đăng nhập bằng Google không
        boolean isGoogleLogin = user.getProvider() != null && user.getProvider().equals("google");
        model.addAttribute("isGoogleLogin", isGoogleLogin);
        
        return "tai-khoan";
    }
    
    /**
     * Cập nhật thông tin tài khoản (phone, fullName)
     */
    @PostMapping("/tai-khoan/cap-nhat")
    public String capNhatThongTin(@RequestParam(required = false) String fullName,
                                  @RequestParam(required = false) String phone,
                                  RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();
        
        if (!userOpt.isPresent()) {
            return "redirect:/dang-ky/tai-khoan";
        }
        
        User user = userOpt.get();
        
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
        }
        
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        
        return "redirect:/tai-khoan";
    }
    
    /**
     * Đổi mật khẩu (chỉ cho user không đăng nhập bằng Google)
     */
    @PostMapping("/tai-khoan/doi-mat-khau")
    public String doiMatKhau(@RequestParam String currentPassword,
                            @RequestParam String newPassword,
                            @RequestParam String confirmPassword,
                            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();
        
        if (!userOpt.isPresent()) {
            return "redirect:/dang-ky/tai-khoan";
        }
        
        User user = userOpt.get();
        
        // Kiểm tra nếu đăng nhập bằng Google thì không cho đổi mật khẩu
        if (user.getProvider() != null && user.getProvider().equals("google")) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản Google không thể đổi mật khẩu.");
            return "redirect:/tai-khoan";
        }
        
        // Kiểm tra mật khẩu hiện tại
        if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/tai-khoan";
        }
        
        // Kiểm tra mật khẩu mới và xác nhận
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận không khớp.");
            return "redirect:/tai-khoan";
        }
        
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "redirect:/tai-khoan";
        }
        
        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        
        return "redirect:/tai-khoan";
    }
    
    /**
     * Thêm địa chỉ mới
     */
    @PostMapping("/tai-khoan/dia-chi/them")
    public String themDiaChi(@RequestParam String fullName,
                             @RequestParam String phone,
                             @RequestParam String province,
                             @RequestParam String district,
                             @RequestParam String ward,
                             @RequestParam String street,
                             @RequestParam(defaultValue = "false") boolean isDefault,
                             RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();
        
        if (!userOpt.isPresent()) {
            return "redirect:/dang-ky/tai-khoan";
        }
        
        User user = userOpt.get();
        
        try {
            // Thêm địa chỉ mới
            Address address = addressService.addAddress(
                user.getId(), fullName, phone, province, district, ward, street
            );
            
            // Nếu user chọn đặt làm mặc định thì set default
            if (isDefault) {
                addressService.setDefaultAddress(user.getId(), address.getId());
            }
            
            redirectAttributes.addFlashAttribute("success", "Thêm địa chỉ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/tai-khoan";
    }
    
    /**
     * Đặt địa chỉ làm mặc định
     */
    @PostMapping("/tai-khoan/dia-chi/mac-dinh")
    public String datDiaChiMacDinh(@RequestParam Long addressId,
                                   RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();
        
        if (!userOpt.isPresent()) {
            return "redirect:/dang-ky/tai-khoan";
        }
        
        User user = userOpt.get();
        
        try {
            addressService.setDefaultAddress(user.getId(), addressId);
            redirectAttributes.addFlashAttribute("success", "Đã đặt địa chỉ làm mặc định!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/tai-khoan";
    }
}

