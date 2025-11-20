package com.example.asmproject.controller;

import com.example.asmproject.model.*;
import com.example.asmproject.service.*;
import com.example.asmproject.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý các trang chính của website
 * Bao gồm: Trang chủ, Chi tiết sản phẩm, Giỏ hàng
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Controller
public class HomeController {

    private static final String TRANG_DANG_CHON = "trangDangChon";
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Trang chủ - Hiển thị danh sách sản phẩm từ database
     * Lấy tất cả sản phẩm đang hoạt động và hiển thị lên giao diện
     * 
     * @param moHinh Model để truyền dữ liệu vào view
     * @return Tên template trang chủ
     */
    @GetMapping("/")
    public String hienThiTrangChu(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        
        // Lấy danh sách sản phẩm đang hoạt động từ database
        // Service sẽ query từ database với status = ACTIVE
        List<Product> products = productService.getAllActiveProducts();
        moHinh.addAttribute("products", products);
        
        return "trang-chu";
    }

    /**
     * Trang chi tiết sản phẩm theo slug
     * Hiển thị thông tin sản phẩm, màu sắc, tồn kho, và đánh giá
     * 
     * @param slug Slug của sản phẩm (URL-friendly name)
     * @param moHinh Model để truyền dữ liệu vào view
     * @return Tên template chi tiết sản phẩm
     */
    @GetMapping("/san-pham/{slug}")
    public String hienThiChiTietSanPham(@PathVariable String slug, Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        
        // Lấy thông tin sản phẩm theo slug từ database
        Optional<Product> productOpt = productService.getProductBySlug(slug);
        
        if (!productOpt.isPresent()) {
            // Nếu không tìm thấy sản phẩm thì redirect về trang chủ
            return "redirect:/";
        }
        
        Product product = productOpt.get();
        moHinh.addAttribute("product", product);
        
        // Lấy danh sách màu sắc và tồn kho của sản phẩm
        // Mỗi màu có số lượng tồn kho riêng
        List<ProductColor> productColors = product.getProductColors();
        moHinh.addAttribute("productColors", productColors);
        
        // Lấy danh sách đánh giá của sản phẩm
        // Hiển thị tối đa 10 đánh giá mới nhất
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = reviewService.getProductReviews(product.getId(), pageable).getContent();
        moHinh.addAttribute("reviews", reviews);
        
        // Tính điểm đánh giá trung bình
        Double averageRating = reviewService.getAverageRating(product.getId());
        moHinh.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        
        // Đếm tổng số đánh giá
        long reviewCount = reviewService.getReviewCount(product.getId());
        moHinh.addAttribute("reviewCount", reviewCount);
        
        return "chi-tiet-san-pham";
    }
    
    /**
     * Trang chi tiết sản phẩm theo slug cũ (backward compatibility)
     */
    @GetMapping("/san-pham/vero-x")
    public String hienThiVeroX(Model moHinh) {
        // Redirect đến slug nếu có, nếu không thì dùng template cũ
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        return "chi-tiet-san-pham";
    }

    @GetMapping("/phu-kien")
    public String hienThiPhuKien(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "phu-kien");
        return "phu-kien";
    }

    @GetMapping("/dich-vu-pin")
    public String hienThiDichVuPin(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dich-vu");
        return "dich-vu-pin";
    }

    @GetMapping("/dich-vu-hau-mai")
    public String hienThiDichVuHauMai(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dich-vu");
        return "dich-vu-hau-mai";
    }

    @GetMapping("/ve-chung-toi")
    public String hienThiVeChungToi(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "gioi-thieu");
        return "ve-chung-toi";
    }

    @GetMapping("/tin-tuc")
    public String hienThiTinTuc(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "tin-tuc");
        return "tin-tuc";
    }

    @GetMapping("/dang-ky/lai-thu")
    public String hienThiDangKyLaiThu(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dang-ky");
        return "dang-ky-lai-thu";
    }

    @GetMapping("/dat-mua/xe-may-dien")
    public String hienThiTrangDatMuaXeMay(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        return "dat-mua-xe-may";
    }

    /**
     * Trang giỏ hàng (Checkout)
     * Hiển thị danh sách sản phẩm trong giỏ hàng, địa chỉ, voucher, phương thức vận chuyển
     * 
     * @param moHinh Model để truyền dữ liệu vào view
     * @return Tên template giỏ hàng
     */
    @GetMapping("/gio-hang")
    public String hienThiGioHang(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "gio-hang");
        
        // Lấy cart items từ database nếu user đã đăng nhập
        // Nếu chưa đăng nhập thì hiển thị giỏ hàng trống hoặc từ local storage
        if (securityUtil.isAuthenticated()) {
            Long userId = securityUtil.getCurrentUserId();
            
            if (userId != null) {
                // Lấy danh sách sản phẩm trong giỏ hàng từ database
                List<Cart> cartItems = cartService.getUserCart(userId);
                moHinh.addAttribute("cartItems", cartItems);
                
                // Tính tổng tiền tạm tính
                double subtotal = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getFinalPrice().doubleValue() * item.getQuantity())
                    .sum();
                moHinh.addAttribute("subtotal", subtotal);
                
                // Lấy danh sách địa chỉ của user (tối đa 4 địa chỉ)
                List<Address> userAddresses = addressService.getUserAddresses(userId);
                moHinh.addAttribute("userAddresses", userAddresses);
                
                // Lấy địa chỉ mặc định
                Optional<Address> defaultAddress = addressService.getDefaultAddress(userId);
                defaultAddress.ifPresent(addr -> moHinh.addAttribute("defaultAddress", addr));
            }
        }
        
        // Nếu chưa đăng nhập, cartItems sẽ là null hoặc empty
        // Client có thể lấy từ local storage và đồng bộ sau khi đăng nhập
        
        return "gio-hang";
    }
    
    /**
     * Trang đăng nhập
     * Hiển thị form đăng nhập và nút đăng nhập bằng Google
     * 
     * @param model Model để truyền dữ liệu vào view
     * @param error Tham số error từ URL (nếu có)
     * @param registered Tham số registered từ URL (nếu có) - thông báo đăng ký thành công
     * @return Tên template đăng nhập
     */
    @GetMapping("/login")
    public String hienThiTrangDangNhap(Model moHinh, 
                                      @org.springframework.web.bind.annotation.RequestParam(required = false) String error,
                                      @org.springframework.web.bind.annotation.RequestParam(required = false) String registered) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dang-nhap");
        moHinh.addAttribute("isLogin", true); // Đánh dấu đây là trang đăng nhập
        
        // Kiểm tra nếu có lỗi thì hiển thị thông báo
        if (error != null) {
            if ("oauth_failed".equals(error)) {
                moHinh.addAttribute("error", "Đăng nhập Google thất bại. Vui lòng thử lại.");
            } else if ("true".equals(error)) {
                moHinh.addAttribute("error", "Email hoặc mật khẩu không đúng. Vui lòng thử lại.");
            } else {
                moHinh.addAttribute("error", "Đăng nhập thất bại. Vui lòng thử lại.");
            }
        }
        
        // Thông báo đăng ký thành công
        if (registered != null && "true".equals(registered)) {
            moHinh.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        
        return "dang-ky-tai-khoan";
    }
    
    /**
     * Trang đăng ký
     * Hiển thị form đăng ký
     */
    @GetMapping("/dang-ky/tai-khoan")
    public String hienThiDangKyTaiKhoan(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dang-ky");
        moHinh.addAttribute("isLogin", false); // Đánh dấu đây là trang đăng ký
        return "dang-ky-tai-khoan";
    }
}

