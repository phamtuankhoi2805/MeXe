
package com.example.asmproject.controller;

import com.example.asmproject.model.Order;
import com.example.asmproject.repository.OrderRepository;
import com.example.asmproject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

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

        // Chart Data: Revenue last 7 days
        List<String> revenueLabels = new ArrayList<>();
        List<Double> revenueData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Initialize map with 0 for last 7 days
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dailyRevenue.put(date.format(formatter), 0.0);
        }

        // Fetch delivered orders in last 7 days
        List<Order> recentOrders = orderRepository.findByOrderStatusAndCreatedAtAfter(
                Order.OrderStatus.DELIVERED, LocalDateTime.now().minusDays(7));

        for (Order order : recentOrders) {
            String dateKey = order.getCreatedAt().format(formatter);
            if (dailyRevenue.containsKey(dateKey)) {
                dailyRevenue.put(dateKey, dailyRevenue.get(dateKey) + order.getTotal().doubleValue());
            }
        }

        revenueLabels.addAll(dailyRevenue.keySet());
        revenueData.addAll(dailyRevenue.values());

        model.addAttribute("revenueLabels", revenueLabels);
        model.addAttribute("revenueData", revenueData);

        // Chart Data: Order Status
        List<Long> orderStatusData = List.of(
                pendingOrders,
                orderService.countOrdersByStatus(Order.OrderStatus.SHIPPING),
                deliveredOrders,
                orderService.countOrdersByStatus(Order.OrderStatus.CANCELLED));
        model.addAttribute("orderStatusData", orderStatusData);

        // Recent Orders
        Pageable top5 = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<Order> recentOrderList = orderRepository.findAll(top5).getContent();
        model.addAttribute("recentOrders", recentOrderList);

        return "admin/dashboard";
    }

    @GetMapping("/san-pham")
    public String quanLySanPham(Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) com.example.asmproject.model.Product.ProductStatus status,
            @RequestParam(defaultValue = "0") int page) {
        model.addAttribute(SECTION_KEY, "san-pham");

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<com.example.asmproject.model.Product> products = productService.searchProducts(keyword, brandId,
                categoryId, status, pageable);

        model.addAttribute("products", products);
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("categories", categoryService.getAllCategories());

        // Preserve filter params
        model.addAttribute("keyword", keyword);
        model.addAttribute("brandId", brandId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);

        return "admin/san-pham";
    }

    @GetMapping("/san-pham/add")
    public String themSanPham(Model model) {
        model.addAttribute(SECTION_KEY, "san-pham");
        model.addAttribute("productRequest", new com.example.asmproject.dto.ProductRequest());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/san-pham-form";
    }

    @GetMapping("/san-pham/edit/{id}")
    public String suaSanPham(@PathVariable Long id, Model model) {
        model.addAttribute(SECTION_KEY, "san-pham");
        com.example.asmproject.model.Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        com.example.asmproject.dto.ProductRequest request = new com.example.asmproject.dto.ProductRequest();
        request.setName(product.getName());
        request.setSlug(product.getSlug());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setDiscountPrice(product.getDiscountPrice());
        request.setQuantity(product.getQuantity());
        request.setImage(product.getImage());
        request.setImages(product.getImages());
        request.setSpecifications(product.getSpecifications());
        request.setStatus(product.getStatus());
        if (product.getBrand() != null)
            request.setBrandId(product.getBrand().getId());
        if (product.getCategory() != null)
            request.setCategoryId(product.getCategory().getId());

        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id);
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/san-pham-form";
    }

    @PostMapping("/san-pham/save")
    public String luuSanPham(@ModelAttribute("productRequest") com.example.asmproject.dto.ProductRequest request,
            @RequestParam(required = false) Long id,
            RedirectAttributes redirectAttributes) {
        try {
            if (id != null) {
                productService.updateProduct(id, request);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công");
            } else {
                productService.createProduct(request);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/san-pham/add"; // Or back to edit if id present, but simple redirect for now
        }
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/san-pham/delete/{id}")
    public String xoaSanPham(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/don-hang")
    public String quanLyDonHang(Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page) {
        model.addAttribute(SECTION_KEY, "don-hang");

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.searchOrders(keyword, status, null, null, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);

        return "admin/don-hang";
    }

    @GetMapping("/don-hang/{id}")
    public String chiTietDonHang(@PathVariable Long id, Model model) {
        model.addAttribute(SECTION_KEY, "don-hang");

        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        model.addAttribute("order", order);
        return "admin/don-hang-chi-tiet";
    }

    @PostMapping("/don-hang/{id}/update-status")
    public String capNhatTrangThaiDonHang(@PathVariable Long id,
            @RequestParam Order.OrderStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }

    @PostMapping("/don-hang/{id}/confirm")
    public String xacNhanDonHang(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, Order.OrderStatus.CONFIRMED);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/don-hang";
    }

    @GetMapping("/thuong-hieu")
    public String quanLyThuongHieu(Model model) {
        model.addAttribute(SECTION_KEY, "thuong-hieu");
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("newBrand", new com.example.asmproject.model.Brand());
        return "admin/thuong-hieu";
    }

    @PostMapping("/thuong-hieu/save")
    public String luuThuongHieu(@ModelAttribute("newBrand") com.example.asmproject.model.Brand brand,
            RedirectAttributes redirectAttributes) {
        try {
            brandService.saveBrand(brand);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thương hiệu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/thuong-hieu";
    }

    @GetMapping("/thuong-hieu/delete/{id}")
    public String xoaThuongHieu(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thương hiệu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/thuong-hieu";
    }

    @GetMapping("/danh-muc")
    public String quanLyDanhMuc(Model model) {
        model.addAttribute(SECTION_KEY, "danh-muc");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newCategory", new com.example.asmproject.model.Category());
        return "admin/danh-muc";
    }

    @PostMapping("/danh-muc/save")
    public String luuDanhMuc(@ModelAttribute("newCategory") com.example.asmproject.model.Category category,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/danh-muc";
    }

    @GetMapping("/danh-muc/delete/{id}")
    public String xoaDanhMuc(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/danh-muc";
    }

    @GetMapping("/mau-sac")
    public String quanLyMauSac(Model model) {
        model.addAttribute(SECTION_KEY, "mau-sac");
        return "admin/mau-sac";
    }

    @GetMapping("/voucher")
    public String quanLyVoucher(Model model) {
        model.addAttribute(SECTION_KEY, "voucher");
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        return "admin/voucher";
    }

    @GetMapping("/voucher/add")
    public String themVoucher(Model model) {
        model.addAttribute(SECTION_KEY, "voucher");
        model.addAttribute("voucher", new com.example.asmproject.model.Voucher());
        return "admin/voucher-form";
    }

    @GetMapping("/voucher/edit/{id}")
    public String suaVoucher(@PathVariable Long id, Model model) {
        model.addAttribute(SECTION_KEY, "voucher");
        com.example.asmproject.model.Voucher voucher = voucherService.getVoucherById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        model.addAttribute("voucher", voucher);
        return "admin/voucher-form";
    }

    @PostMapping("/voucher/save")
    public String luuVoucher(@ModelAttribute("voucher") com.example.asmproject.model.Voucher voucher,
            RedirectAttributes redirectAttributes) {
        try {
            voucherService.saveVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu voucher thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/voucher/add";
        }
        return "redirect:/admin/voucher";
    }

    @GetMapping("/voucher/delete/{id}")
    public String xoaVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/voucher";
    }

    @GetMapping("/nguoi-dung")
    public String quanLyNguoiDung(Model model) {
        model.addAttribute(SECTION_KEY, "nguoi-dung");
        return "admin/nguoi-dung";
    }
}
