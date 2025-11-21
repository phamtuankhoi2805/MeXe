package com.example.asmproject.controller;

import com.example.asmproject.model.Order;
import com.example.asmproject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String SECTION_KEY = "adminSection";
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public String hienThiDashboard(Model model) {
        model.addAttribute(SECTION_KEY, "dashboard");
        
        // Statistics for dashboard
        long totalOrders = orderService.countOrdersByStatus(null);
        long pendingOrders = orderService.countOrdersByStatus(Order.OrderStatus.PENDING);
        long deliveredOrders = orderService.countOrdersByStatus(Order.OrderStatus.DELIVERED);
        long totalProducts = productService.getTotalActiveProducts();
        long outOfStockProducts = productService.getTotalOutOfStockProducts();
        long totalUsers = userService.getTotalUsers();
        long activeVouchers = voucherService.getActiveVoucherCount();
        
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        double totalRevenue = orderService.getTotalRevenue(lastWeek).doubleValue();
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "admin/dashboard";
    }

    @GetMapping("/san-pham")
    public String quanLySanPham(Model model) {
        model.addAttribute(SECTION_KEY, "san-pham");
        return "admin/san-pham";
    }

    @GetMapping("/don-hang")
    public String quanLyDonHang(Model model) {
        model.addAttribute(SECTION_KEY, "don-hang");
        return "admin/don-hang";
    }
    
    @GetMapping("/thuong-hieu")
    public String quanLyThuongHieu(Model model) {
        model.addAttribute(SECTION_KEY, "thuong-hieu");
        return "admin/thuong-hieu";
    }
    
    @GetMapping("/danh-muc")
    public String quanLyDanhMuc(Model model) {
        model.addAttribute(SECTION_KEY, "danh-muc");
        return "admin/danh-muc";
    }
    
    @GetMapping("/mau-sac")
    public String quanLyMauSac(Model model) {
        model.addAttribute(SECTION_KEY, "mau-sac");
        return "admin/mau-sac";
    }
    
    @GetMapping("/voucher")
    public String quanLyVoucher(Model model) {
        model.addAttribute(SECTION_KEY, "voucher");
        return "admin/voucher";
    }
    
    @GetMapping("/nguoi-dung")
    public String quanLyNguoiDung(Model model) {
        model.addAttribute(SECTION_KEY, "nguoi-dung");
        return "admin/nguoi-dung";
    }
}

