package com.example.asmproject.controller;

import com.example.asmproject.dto.ProductDetailDTO;
import com.example.asmproject.model.*;
import com.example.asmproject.repository.UserRepository;
import com.example.asmproject.service.*;
import com.example.asmproject.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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

    @Autowired
    private com.example.asmproject.service.ChargingStationService chargingStationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

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
     * Sử dụng DTO và JOIN FETCH để tránh lazy loading exception
     * 
     * @param slug   Slug của sản phẩm (URL-friendly name)
     * @param moHinh Model để truyền dữ liệu vào view
     * @return Tên template chi tiết sản phẩm
     */
    @GetMapping("/san-pham/{slug}")
    public String hienThiChiTietSanPham(@PathVariable String slug, Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");

        // Lấy thông tin sản phẩm theo slug từ database với JOIN FETCH
        // Query này sẽ load brand, category, productColors và color cùng lúc
        Optional<Product> productEntityOpt = productService.getProductBySlugWithDetails(slug);

        if (!productEntityOpt.isPresent()) {
            // Nếu không tìm thấy sản phẩm thì redirect về trang chủ
            return "redirect:/";
        }

        Product productEntity = productEntityOpt.get();

        // Chuyển đổi sang DTO để tránh lazy loading khi render template
        ProductDetailDTO product = new ProductDetailDTO(productEntity);
        moHinh.addAttribute("product", product);

        // Lấy danh sách ảnh phụ từ bảng product_images
        List<String> productImages = productEntity.getProductImages().stream()
                .map(com.example.asmproject.model.ProductImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList());

        // Nếu không có ảnh trong bảng mới, fallback về field cũ (nếu có)
        if (productImages.isEmpty() && productEntity.getImages() != null) {
            String[] images = productEntity.getImages().split(",");
            for (String img : images) {
                String trimmedImg = img.trim();
                if (!trimmedImg.isEmpty() && !productImages.contains(trimmedImg)) {
                    productImages.add(trimmedImg);
                }
            }
        }
        if (productImages.isEmpty() && product.getImage() != null) {
            productImages.add(product.getImage());
        }

        moHinh.addAttribute("productImages", productImages);

        // Lấy danh sách màu sắc và tồn kho của sản phẩm (đã được load bằng JOIN FETCH)
        List<ProductColor> productColors = productEntity.getProductColors();
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

        // Lấy userId nếu đã đăng nhập (để JavaScript sử dụng)
        if (securityUtil.isAuthenticated()) {
            Long userId = securityUtil.getCurrentUserId();
            moHinh.addAttribute("currentUserId", userId);
        }

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

    /**
     * Trang dịch vụ pin - Hiển thị danh sách trạm sạc pin gần nhất
     * 
     * @param moHinh Model để truyền dữ liệu vào view
     * @param lat    Vĩ độ (optional) - nếu không có sẽ lấy tất cả trạm
     * @param lng    Kinh độ (optional) - nếu không có sẽ lấy tất cả trạm
     * @return Tên template dịch vụ pin
     */
    @GetMapping("/dich-vu-pin")
    public String hienThiDichVuPin(Model moHinh,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String lng) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dich-vu");

        // Lấy danh sách trạm sạc pin
        List<com.example.asmproject.model.ChargingStation> stations;

        // Nếu có tọa độ, tìm trạm gần nhất
        if (lat != null && lng != null && !lat.isEmpty() && !lng.isEmpty()) {
            try {
                java.math.BigDecimal latitude = new java.math.BigDecimal(lat);
                java.math.BigDecimal longitude = new java.math.BigDecimal(lng);
                stations = chargingStationService.findNearestStations(latitude, longitude, 10);
            } catch (NumberFormatException e) {
                // Nếu tọa độ không hợp lệ, lấy tất cả trạm
                stations = chargingStationService.getAllActiveStations();
            }
        } else {
            // Nếu không có tọa độ, lấy tất cả trạm đang hoạt động (tối đa 20)
            stations = chargingStationService.getAllActiveStations();
            if (stations.size() > 20) {
                stations = stations.subList(0, 20);
            }
        }

        moHinh.addAttribute("chargingStations", stations);
        moHinh.addAttribute("totalStations", chargingStationService.getAllActiveStations().size());

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
     * Hiển thị danh sách sản phẩm trong giỏ hàng, địa chỉ, voucher, phương thức vận
     * chuyển
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
     * Trang thanh toán tại showroom
     * Hiển thị form điền thông tin và danh sách sản phẩm từ giỏ hàng
     * 
     * @param moHinh Model để truyền dữ liệu vào view
     * @return Tên template thanh toán showroom
     */
    @GetMapping("/thanh-toan-showroom")
    public String hienThiThanhToanShowroom(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "gio-hang");

        // Kiểm tra user đã đăng nhập
        if (!securityUtil.isAuthenticated()) {
            return "redirect:/login?error=login_required";
        }

        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login?error=login_required";
        }

        // Lấy danh sách sản phẩm trong giỏ hàng từ database
        List<Cart> cartItems = cartService.getUserCart(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            // Nếu giỏ hàng trống, redirect về trang giỏ hàng
            moHinh.addAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/gio-hang";
        }

        moHinh.addAttribute("cartItems", cartItems);

        // Tính tổng tiền tạm tính
        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getFinalPrice().doubleValue() * item.getQuantity())
                .sum();
        moHinh.addAttribute("subtotal", subtotal);

        // Lấy thông tin user hiện tại
        if (securityUtil.isAuthenticated()) {
            Long currentUserId = securityUtil.getCurrentUserId();
            if (currentUserId != null) {
                Optional<User> userOpt = java.util.Optional.empty();
                try {
                    userOpt = userRepository.findById(currentUserId);
                    userOpt.ifPresent(user -> moHinh.addAttribute("currentUser", user));

                    // Lấy danh sách địa chỉ của user
                    List<Address> userAddresses = addressService.getUserAddresses(currentUserId);
                    moHinh.addAttribute("userAddresses", userAddresses);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return "thanh-toan-showroom";
    }

    /**
     * Xử lý thanh toán tại showroom
     * Tạo đơn hàng với phương thức thanh toán tại showroom
     * 
     * @param fullName           Họ và tên người nhận
     * @param phone              Số điện thoại
     * @param deliveryAddress    Địa chỉ giao xe (đầy đủ)
     * @param deliveryMethod     Phương thức vận chuyển
     * @param moHinh             Model để truyền dữ liệu vào view
     * @param redirectAttributes Redirect attributes để truyền thông báo
     * @return Redirect về trang đơn hàng hoặc trang giỏ hàng nếu có lỗi
     */
    @PostMapping("/thanh-toan-showroom")
    public String xuLyThanhToanShowroom(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String deliveryAddress,
            @RequestParam(required = false, defaultValue = "STANDARD") String deliveryMethod,
            Model moHinh,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        // Kiểm tra user đã đăng nhập
        if (!securityUtil.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thanh toán.");
            return "redirect:/login";
        }

        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thanh toán.");
            return "redirect:/login";
        }

        try {
            // Parse địa chỉ thành các thành phần
            // Format: "Số nhà, đường, quận/huyện, tỉnh/thành"
            String[] addressParts = deliveryAddress.split(",");
            String street = addressParts.length > 0 ? addressParts[0].trim() : deliveryAddress.trim();
            String ward = addressParts.length > 1 ? addressParts[1].trim() : "Không xác định";
            String district = addressParts.length > 2 ? addressParts[2].trim() : "Không xác định";
            String province = addressParts.length > 3 ? addressParts[3].trim() : "Không xác định";

            // Tạo hoặc lấy địa chỉ
            Address address;
            List<Address> existingAddresses = addressService.getUserAddresses(userId);

            // Kiểm tra xem có địa chỉ tương tự không
            Optional<Address> similarAddress = existingAddresses.stream()
                    .filter(addr -> addr.getStreet().equals(street) &&
                            addr.getDistrict().equals(district) &&
                            addr.getProvince().equals(province))
                    .findFirst();

            if (similarAddress.isPresent()) {
                address = similarAddress.get();
            } else {
                // Tạo địa chỉ mới
                try {
                    address = addressService.addAddress(
                            userId, fullName.trim(), phone.trim(),
                            province, district, ward, street);
                } catch (RuntimeException e) {
                    // Nếu đã có 4 địa chỉ, dùng địa chỉ đầu tiên
                    if (e.getMessage().contains("tối đa")) {
                        address = existingAddresses.isEmpty() ? null : existingAddresses.get(0);
                        if (address == null) {
                            throw new RuntimeException("Không thể tạo đơn hàng. Vui lòng thêm địa chỉ trước.");
                        }
                    } else {
                        throw e;
                    }
                }
            }

            if (address == null) {
                throw new RuntimeException("Không thể tạo địa chỉ.");
            }

            // Tạo đơn hàng
            Order.DeliveryMethod delivery = Order.DeliveryMethod.valueOf(deliveryMethod.toUpperCase());
            Order order = orderService.createOrder(
                    userId,
                    address.getId(),
                    null, // Không dùng voucher trong thanh toán showroom
                    "SHOWROOM", // Payment method
                    delivery);

            // Redirect đến trang xác nhận đơn hàng
            return "redirect:/xac-nhan-don-hang?orderCode=" + order.getOrderCode();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.");
            return "redirect:/thanh-toan-showroom";
        }
    }

    /**
     * Trang xác nhận đơn hàng
     * Hiển thị thông tin chi tiết đơn hàng sau khi thanh toán thành công
     * 
     * @param orderCode Mã đơn hàng để tìm đơn hàng
     * @param moHinh    Model để truyền dữ liệu vào view
     * @return Tên template xác nhận đơn hàng
     */
    @GetMapping("/xac-nhan-don-hang")
    public String hienThiXacNhanDonHang(@RequestParam(required = false) String orderCode, Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "gio-hang");

        // Kiểm tra user đã đăng nhập
        if (!securityUtil.isAuthenticated()) {
            return "redirect:/login?error=login_required";
        }

        Long userId = securityUtil.getCurrentUserId();
        if (userId == null || orderCode == null || orderCode.trim().isEmpty()) {
            moHinh.addAttribute("error", "Mã đơn hàng không hợp lệ.");
            return "xac-nhan-don-hang";
        }

        try {
            // Tìm đơn hàng theo orderCode
            Optional<Order> orderOpt = orderService.getOrderByCode(orderCode.trim());

            if (!orderOpt.isPresent()) {
                moHinh.addAttribute("error", "Không tìm thấy đơn hàng với mã: " + orderCode);
                return "xac-nhan-don-hang";
            }

            Order order = orderOpt.get();

            // Kiểm tra đơn hàng có thuộc về user hiện tại không
            if (!order.getUser().getId().equals(userId) && !securityUtil.isAdmin()) {
                moHinh.addAttribute("error", "Bạn không có quyền xem đơn hàng này.");
                return "xac-nhan-don-hang";
            }

            // Load order items để tránh lazy loading
            order.getOrderItems().size();
            order.getAddress().getFullAddress();

            moHinh.addAttribute("order", order);
            moHinh.addAttribute("success", "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderCode());

        } catch (Exception e) {
            moHinh.addAttribute("error", "Có lỗi xảy ra khi tải thông tin đơn hàng: " + e.getMessage());
        }

        return "xac-nhan-don-hang";
    }

    /**
     * Trang đăng nhập
     * Hiển thị form đăng nhập và nút đăng nhập bằng Google
     * 
     * @param model      Model để truyền dữ liệu vào view
     * @param error      Tham số error từ URL (nếu có)
     * @param registered Tham số registered từ URL (nếu có) - thông báo đăng ký
     *                   thành công
     * @return Tên template đăng nhập
     */
    @GetMapping("/login")
    public String hienThiTrangDangNhap(Model moHinh,
            HttpServletRequest request,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String error,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String registered) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dang-nhap");
        moHinh.addAttribute("isLogin", true); // Đánh dấu đây là trang đăng nhập

        // Kiểm tra nếu có lỗi thì hiển thị thông báo
        if (error != null) {
            String message = request.getParameter("message");
            if (message != null && !message.isEmpty()) {
                // Sử dụng thông báo từ URL parameter
                moHinh.addAttribute("error", message);
            } else if ("oauth_failed".equals(error)) {
                moHinh.addAttribute("error", "Đăng nhập Google thất bại. Vui lòng thử lại.");
            } else if ("email_not_verified".equals(error)) {
                moHinh.addAttribute("error",
                        "Email chưa được xác thực. Vui lòng kiểm tra email và nhập mã xác nhận 6 số.");
            } else if ("account_disabled".equals(error)) {
                moHinh.addAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            } else if ("bad_credentials".equals(error) || "true".equals(error)) {
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

    /**
     * Trang tìm kiếm sản phẩm
     * Hiển thị kết quả tìm kiếm dựa trên từ khóa
     * Tìm kiếm trong: tên sản phẩm, mô tả, slug, tên thương hiệu, tên danh mục
     */
    @GetMapping("/tim-kiem")
    public String hienThiTimKiem(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm sản phẩm theo từ khóa với phân trang
            Pageable pageable = PageRequest.of(page, size);
            var productsPage = productService.searchProducts(
                    keyword.trim(),
                    null,
                    null,
                    Product.ProductStatus.ACTIVE,
                    pageable);

            moHinh.addAttribute("products", productsPage.getContent());
            moHinh.addAttribute("keyword", keyword.trim());
            moHinh.addAttribute("totalResults", productsPage.getTotalElements());
            moHinh.addAttribute("totalPages", productsPage.getTotalPages());
            moHinh.addAttribute("currentPage", productsPage.getNumber());
            moHinh.addAttribute("hasNext", productsPage.hasNext());
            moHinh.addAttribute("hasPrevious", productsPage.hasPrevious());
        } else {
            // Nếu không có từ khóa, không hiển thị sản phẩm
            moHinh.addAttribute("products", new ArrayList<>());
            moHinh.addAttribute("totalResults", 0L);
        }

        return "tim-kiem";
    }

    /**
     * Trang thông báo không có quyền truy cập (403)
     * Hiển thị khi user không có quyền truy cập trang admin
     */
    @GetMapping("/khong-co-quyen")
    public String hienThiKhongCoQuyen(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "khong-co-quyen");
        return "khong-co-quyen";
    }

    /**
     * Trang xác thực email
     * Hiển thị form để người dùng nhập mã xác nhận 6 số
     */
    @GetMapping("/verify-email")
    public String hienThiVerifyEmail(@RequestParam(required = false) String email, Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "verify-email");
        moHinh.addAttribute("email", email);
        return "verify-email";
    }
}
