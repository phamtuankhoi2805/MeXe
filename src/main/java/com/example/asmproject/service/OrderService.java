package com.example.asmproject.service;

import com.example.asmproject.dto.*;
import com.example.asmproject.model.*;
import com.example.asmproject.model.enums.PaymentStatus;
import com.example.asmproject.model.enums.ShippingStatus;
import com.example.asmproject.model.enums.ShippingType;
import com.example.asmproject.repository.*;
import com.example.asmproject.service.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderMapper orderMapper;

    public Order createOrder(Long userId, Long addressId, String voucherCode,
            String paymentMethod, Order.DeliveryMethod deliveryMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Địa chỉ không thuộc về người dùng này");
        }

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // Calculate subtotal
        BigDecimal subtotal = cartItems.stream()
                .map(cart -> cart.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply voucher
        BigDecimal discount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            Optional<Voucher> voucherOpt = voucherRepository.findActiveByCode(voucherCode);
            if (voucherOpt.isPresent()) {
                voucher = voucherOpt.get();
                discount = voucher.calculateDiscount(subtotal);
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    voucher.setUsedCount(voucher.getUsedCount() + 1);
                    voucherRepository.save(voucher);
                }
            }
        }

        // Calculate shipping fee
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (deliveryMethod == Order.DeliveryMethod.FAST) {
            shippingFee = new BigDecimal("50000"); // 50k for fast delivery
        }

        // Calculate total
        BigDecimal total = subtotal.subtract(discount).add(shippingFee);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setVoucher(voucher);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shippingFee);
        order.setTotal(total);
        order.setPaymentMethod(paymentMethod);
        order.setDeliveryMethod(deliveryMethod);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderStatus(Order.OrderStatus.PENDING);
        order.generateOrderCode();

        order = orderRepository.save(order);

        // Create order items and update product quantities
        for (Cart cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setColor(cartItem.getColor());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setProductImage(cartItem.getProduct().getImage());
            orderItem.setColorName(cartItem.getColor() != null ? cartItem.getColor().getName() : null);
            orderItem.setPrice(cartItem.getProduct().getFinalPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(cartItem.getProduct().getFinalPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemRepository.save(orderItem);

            // Update product quantity
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            if (product.getQuantity() <= 0) {
                product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);
        }

        // Clear cart
        cartRepository.deleteByUserId(userId);

        return order;
    }

    public Order repurchaseOrder(Long userId, Long orderId) {
        Order originalOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!originalOrder.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền mua lại đơn hàng này");
        }

        // Add items back to cart
        for (OrderItem item : originalOrder.getOrderItems()) {
            Product product = item.getProduct();
            if (product.isInStock()) {
                cartService.addToCart(userId, product.getId(),
                        item.getColor() != null ? item.getColor().getId() : null,
                        item.getQuantity());
            }
        }

        return originalOrder;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Lấy chi tiết đơn hàng trả về dạng Response DTO.
     * Sử dụng cho các API hiển thị chi tiết đơn hàng.
     */
    public Optional<OrderResponse> getOrderResponseById(Long id) {
        return orderRepository.findById(id).map(orderMapper::toResponse);
    }

    public Optional<Order> getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    public Page<Order> searchOrders(String keyword, Order.OrderStatus orderStatus,
            Order.PaymentStatus paymentStatus,
            Order.DeliveryMethod deliveryMethod,
            Pageable pageable) {
        return orderRepository.searchOrders(keyword, orderStatus, paymentStatus, deliveryMethod, pageable);
    }

    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setOrderStatus(status);
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    public OrderResponse updatePaymentStatus(Long orderId, Order.PaymentStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setPaymentStatus(status);
        if (status == Order.PaymentStatus.PAID) {
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        }
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    public OrderResponse updateFastDeliveryStatus(Long orderId, String status, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (order.getDeliveryMethod() != Order.DeliveryMethod.FAST) {
            throw new RuntimeException("Đơn hàng này không phải giao hàng nhanh");
        }

        order.setFastDeliveryStatus(status);
        if (trackingNumber != null) {
            order.setTrackingNumber(trackingNumber);
        }
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getFastDeliveryOrders() {
        return orderRepository.findFastDeliveryOrders().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public long countOrdersByStatus(Order.OrderStatus status) {
        if (status == null) {
            return orderRepository.count();
        }
        return orderRepository.countByOrderStatus(status);
    }

    public BigDecimal getTotalRevenue(LocalDateTime startDate) {
        Double total = orderRepository.sumTotalByDeliveredAndDateAfter(startDate);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }

    /**
     * Tìm kiếm đơn hàng với các tham số mới
     */
    public Page<OrderResponse> searchOrders(String keyword, ShippingType shippingType,
            PaymentStatus paymentStatus,
            ShippingStatus shippingStatus,
            LocalDate fromDate,
            LocalDate toDate,
            PageRequest pageable) {
        Specification<Order> spec = Specification.where(null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordLower = keyword.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("orderCode")), "%" + keywordLower + "%"),
                    cb.like(cb.lower(root.join("user").get("email")), "%" + keywordLower + "%")));
        }

        if (shippingType != null) {
            Order.DeliveryMethod deliveryMethod = shippingType == ShippingType.GIAO_NHANH
                    ? Order.DeliveryMethod.FAST
                    : Order.DeliveryMethod.STANDARD;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("deliveryMethod"), deliveryMethod));
        }

        if (paymentStatus != null) {
            Order.PaymentStatus orderPaymentStatus = convertPaymentStatus(paymentStatus);
            if (orderPaymentStatus != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), orderPaymentStatus));
            }
        }

        if (shippingStatus != null) {
            Order.OrderStatus orderStatus = convertShippingStatus(shippingStatus);
            if (orderStatus != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("orderStatus"), orderStatus));
            }
        }

        if (fromDate != null) {
            LocalDateTime fromDateTime = fromDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
        }

        if (toDate != null) {
            LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(orderMapper::toResponse);
    }

    /**
     * Tạo đơn hàng từ OrderRequest
     */
    public OrderResponse createOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Get first address for user (simplified - in real app, you'd select address)
        List<Address> addresses = addressRepository.findByUserId(request.getUserId());
        if (addresses.isEmpty()) {
            throw new RuntimeException("Người dùng chưa có địa chỉ");
        }
        Address address = addresses.get(0);

        // Convert ShippingType to DeliveryMethod
        Order.DeliveryMethod deliveryMethod = request.getShippingType() == ShippingType.GIAO_NHANH
                ? Order.DeliveryMethod.FAST
                : Order.DeliveryMethod.STANDARD;

        // Calculate totals
        BigDecimal subtotal = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = deliveryMethod == Order.DeliveryMethod.FAST
                ? new BigDecimal("50000")
                : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shippingFee);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotal(total);
        order.setPaymentMethod("CASH"); // Default
        order.setDeliveryMethod(deliveryMethod);
        order.setPaymentStatus(convertPaymentStatus(request.getPaymentStatus()));
        order.setOrderStatus(Order.OrderStatus.PENDING);
        order.setNotes(request.getNote());
        order.generateOrderCode();

        order = orderRepository.save(order);

        // Create order items
        for (OrderItemRequest itemRequest : request.getItems()) {
            // Find product by name (note: this assumes unique product names, which may not
            // be ideal)
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals(itemRequest.getProductName()))
                    .collect(Collectors.toList());

            Product product = products.isEmpty()
                    ? null
                    : products.get(0);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            if (product != null) {
                orderItem.setProduct(product);
                orderItem.setProductImage(product.getImage());
                // Update product quantity
                product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
                if (product.getQuantity() <= 0) {
                    product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
                }
                productRepository.save(product);
            }
            orderItem.setProductName(itemRequest.getProductName());
            orderItem.setPrice(itemRequest.getUnitPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubtotal(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            orderItemRepository.save(orderItem);
        }

        return orderMapper.toResponse(order);
    }

    /**
     * Lấy trạng thái giao hàng
     */
    public ShippingStatusResponse getShippingStatus(String code) {
        Order order = orderRepository.findByOrderCode(code)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        ShippingStatusResponse response = new ShippingStatusResponse();
        response.setOrderCode(order.getOrderCode());
        response.setShippingType(order.getDeliveryMethod() == Order.DeliveryMethod.FAST
                ? ShippingType.GIAO_NHANH
                : ShippingType.TIEU_CHUAN);
        response.setShippingStatus(convertOrderStatusToShippingStatus(order.getOrderStatus()));
        response.setLastUpdated(order.getUpdatedAt());

        return response;
    }

    /**
     * Xây dựng báo cáo đơn hàng
     */
    public OrderReportResponse buildReport(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : LocalDateTime.now();

        Specification<Order> spec = (root, query, cb) -> cb.between(root.get("createdAt"), fromDateTime, toDateTime);
        List<Order> orders = orderRepository.findAll(spec);

        OrderReportResponse report = new OrderReportResponse();
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setTotalOrders(orders.size());

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(totalRevenue);

        long fastShipping = orders.stream()
                .filter(o -> o.getDeliveryMethod() == Order.DeliveryMethod.FAST)
                .count();
        report.setFastShippingOrders(fastShipping);

        report.setStandardShippingOrders(orders.size() - fastShipping);

        return report;
    }

    /**
     * Xây dựng báo cáo CSV
     */
    public String buildReportCsv(LocalDate fromDate, LocalDate toDate) {
        OrderReportResponse report = buildReport(fromDate, toDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Từ ngày,Tới ngày,Tổng đơn hàng,Doanh thu,Đơn giao nhanh,Đơn giao tiêu chuẩn\n");
        csv.append(String.format("%s,%s,%d,%s,%d,%d\n",
                report.getFromDate() != null ? report.getFromDate().toString() : "",
                report.getToDate() != null ? report.getToDate().toString() : "",
                report.getTotalOrders(),
                report.getTotalRevenue().toString(),
                report.getFastShippingOrders(),
                report.getStandardShippingOrders()));

        return csv.toString();
    }

    // Helper methods for enum conversion
    private Order.PaymentStatus convertPaymentStatus(PaymentStatus status) {
        if (status == null)
            return null;
        switch (status) {
            case CHO_THANH_TOAN:
                return Order.PaymentStatus.PENDING;
            case DA_THANH_TOAN:
                return Order.PaymentStatus.PAID;
            case THAT_BAI:
                return Order.PaymentStatus.FAILED;
            default:
                return Order.PaymentStatus.PENDING;
        }
    }

    private Order.OrderStatus convertShippingStatus(ShippingStatus status) {
        if (status == null)
            return null;
        switch (status) {
            case CHO_XU_LY:
                return Order.OrderStatus.PENDING;
            case DANG_DONG_GOI:
                return Order.OrderStatus.PROCESSING;
            case DANG_GIAO:
                return Order.OrderStatus.SHIPPING;
            case DA_GIAO:
                return Order.OrderStatus.DELIVERED;
            case DA_HUY:
                return Order.OrderStatus.CANCELLED;
            default:
                return Order.OrderStatus.PENDING;
        }
    }

    private ShippingStatus convertOrderStatusToShippingStatus(Order.OrderStatus status) {
        if (status == null)
            return null;
        switch (status) {
            case PENDING:
            case CONFIRMED:
                return ShippingStatus.CHO_XU_LY;
            case PROCESSING:
                return ShippingStatus.DANG_DONG_GOI;
            case SHIPPING:
                return ShippingStatus.DANG_GIAO;
            case DELIVERED:
                return ShippingStatus.DA_GIAO;
            case CANCELLED:
            case RETURNED:
                return ShippingStatus.DA_HUY;
            default:
                return ShippingStatus.CHO_XU_LY;
        }
    }
}
