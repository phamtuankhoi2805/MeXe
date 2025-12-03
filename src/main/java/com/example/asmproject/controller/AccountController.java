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
     * Kiểm tra nếu user mới (chưa có phone hoặc address) thì hiển thị form nhập
     * thông tin
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
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để cập nhật thông tin.");
            return "redirect:/login";
        }

        User user = userOpt.get();

        // Validate fullName
        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Họ và tên không được để trống.");
            return "redirect:/tai-khoan";
        }

        if (fullName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("error", "Họ và tên phải có ít nhất 2 ký tự.");
            return "redirect:/tai-khoan";
        }

        if (fullName.trim().length() > 100) {
            redirectAttributes.addFlashAttribute("error", "Họ và tên không được vượt quá 100 ký tự.");
            return "redirect:/tai-khoan";
        }

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại không được để trống.");
            return "redirect:/tai-khoan";
        }

        String phoneDigits = phone.replaceAll("[^0-9]", "");
        if (phoneDigits.length() < 10 || phoneDigits.length() > 11) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ. Vui lòng nhập từ 10-11 chữ số.");
            return "redirect:/tai-khoan";
        }

        try {
            user.setFullName(fullName.trim());
            user.setPhone(phoneDigits);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin. Vui lòng thử lại.");
        }

        return "redirect:/tai-khoan";
    }

    /**
     * Đổi mật khẩu (chỉ cho user không đăng nhập bằng Google)
     */
    @PostMapping("/tai-khoan/doi-mat-khau")
    public String doiMatKhau(@RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();

        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để đổi mật khẩu.");
            return "redirect:/login";
        }

        User user = userOpt.get();

        // Kiểm tra nếu đăng nhập bằng Google thì không cho đổi mật khẩu
        if (user.getProvider() != null && user.getProvider().equals("google")) {
            redirectAttributes.addFlashAttribute("error",
                    "Tài khoản đăng nhập bằng Google không thể đổi mật khẩu. Vui lòng quản lý mật khẩu trên tài khoản Google của bạn.");
            return "redirect:/tai-khoan";
        }

        // Validate current password
        if (currentPassword == null || currentPassword.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại.");
            return "redirect:/tai-khoan";
        }

        // Kiểm tra mật khẩu hiện tại
        if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng. Vui lòng kiểm tra lại.");
            return "redirect:/tai-khoan";
        }

        // Validate new password
        if (newPassword == null || newPassword.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu mới.");
            return "redirect:/tai-khoan";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/tai-khoan";
        }

        if (newPassword.length() > 50) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được vượt quá 50 ký tự.");
            return "redirect:/tai-khoan";
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng xác nhận mật khẩu mới.");
            return "redirect:/tai-khoan";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận không khớp. Vui lòng nhập lại.");
            return "redirect:/tai-khoan";
        }

        // Kiểm tra mật khẩu mới không được trùng với mật khẩu cũ
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải khác mật khẩu hiện tại.");
            return "redirect:/tai-khoan";
        }

        try {
            // Cập nhật mật khẩu
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success",
                    "Đổi mật khẩu thành công! Vui lòng đăng nhập lại với mật khẩu mới.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại.");
        }

        return "redirect:/tai-khoan";
    }

    /**
     * Thêm địa chỉ mới
     */
    @PostMapping("/tai-khoan/dia-chi/them")
    public String themDiaChi(@RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String street,
            @RequestParam(defaultValue = "false") boolean isDefault,
            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();

        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thêm địa chỉ.");
            return "redirect:/login";
        }

        User user = userOpt.get();

        // Validate fullName
        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Họ tên người nhận không được để trống.");
            return "redirect:/tai-khoan";
        }

        if (fullName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("error", "Họ tên người nhận phải có ít nhất 2 ký tự.");
            return "redirect:/tai-khoan";
        }

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại không được để trống.");
            return "redirect:/tai-khoan";
        }

        String phoneDigits = phone.replaceAll("[^0-9]", "");
        if (phoneDigits.length() < 10 || phoneDigits.length() > 11) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ. Vui lòng nhập từ 10-11 chữ số.");
            return "redirect:/tai-khoan";
        }

        // Validate province
        if (province == null || province.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn Tỉnh/Thành phố.");
            return "redirect:/tai-khoan";
        }

        // Validate district
        if (district == null || district.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn Quận/Huyện.");
            return "redirect:/tai-khoan";
        }

        // Validate ward
        if (ward == null || ward.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn Phường/Xã.");
            return "redirect:/tai-khoan";
        }

        // Validate street
        if (street == null || street.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ cụ thể không được để trống.");
            return "redirect:/tai-khoan";
        }

        if (street.trim().length() < 5) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ cụ thể phải có ít nhất 5 ký tự.");
            return "redirect:/tai-khoan";
        }

        try {
            // Thêm địa chỉ mới
            Address address = addressService.addAddress(
                    user.getId(), fullName.trim(), phoneDigits, province.trim(), district.trim(), ward.trim(),
                    street.trim());

            // Nếu user chọn đặt làm mặc định thì set default
            if (isDefault) {
                addressService.setDefaultAddress(user.getId(), address.getId());
            }

            redirectAttributes.addFlashAttribute("success", "Thêm địa chỉ thành công!");
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Có lỗi xảy ra khi thêm địa chỉ. Vui lòng thử lại.";
            }
            redirectAttributes.addFlashAttribute("error", errorMsg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm địa chỉ. Vui lòng thử lại.");
        }

        return "redirect:/tai-khoan";
    }

    @Autowired
    private com.example.asmproject.service.OrderService orderService;

    /**
     * Trang danh sách đơn hàng của tôi
     */
    @GetMapping("/tai-khoan/don-hang")
    public String hienThiDonHang(Model model) {
        Optional<User> userOpt = securityUtil.getCurrentUser();

        if (!userOpt.isPresent()) {
            return "redirect:/dang-ky/tai-khoan";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("trangDangChon", "tai-khoan");

        // Lấy danh sách đơn hàng
        List<com.example.asmproject.model.Order> orders = orderService.getUserOrders(user.getId());
        model.addAttribute("orders", orders);

        return "tai-khoan-don-hang";
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @PostMapping("/tai-khoan/dia-chi/mac-dinh")
    public String datDiaChiMacDinh(@RequestParam(required = false) Long addressId,
            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();

        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện thao tác này.");
            return "redirect:/login";
        }

        User user = userOpt.get();

        if (addressId == null) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ không hợp lệ.");
            return "redirect:/tai-khoan";
        }

        try {
            addressService.setDefaultAddress(user.getId(), addressId);
            redirectAttributes.addFlashAttribute("success", "Đã đặt địa chỉ làm mặc định!");
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Có lỗi xảy ra khi đặt địa chỉ mặc định. Vui lòng thử lại.";
            }
            redirectAttributes.addFlashAttribute("error", errorMsg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đặt địa chỉ mặc định. Vui lòng thử lại.");
        }

        return "redirect:/tai-khoan";
    }

    /**
     * Xóa địa chỉ
     */
    @PostMapping("/tai-khoan/dia-chi/xoa")
    public String xoaDiaChi(@RequestParam(required = false) Long addressId,
            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = securityUtil.getCurrentUser();

        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện thao tác này.");
            return "redirect:/login";
        }

        User user = userOpt.get();

        if (addressId == null) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ không hợp lệ.");
            return "redirect:/tai-khoan";
        }

        try {
            addressService.deleteAddress(user.getId(), addressId);
            redirectAttributes.addFlashAttribute("success", "Xóa địa chỉ thành công!");
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Có lỗi xảy ra khi xóa địa chỉ. Vui lòng thử lại.";
            }
            redirectAttributes.addFlashAttribute("error", errorMsg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa địa chỉ. Vui lòng thử lại.");
        }

        return "redirect:/tai-khoan";
    }
}
