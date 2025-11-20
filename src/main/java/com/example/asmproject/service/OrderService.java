package com.example.asmproject.service;

import com.example.asmproject.model.*;
import com.example.asmproject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    
    public Optional<Order> getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }
    
    public Page<Order> searchOrders(String keyword, Order.OrderStatus orderStatus,
                                   Order.PaymentStatus paymentStatus,
                                   Order.DeliveryMethod deliveryMethod,
                                   Pageable pageable) {
        return orderRepository.searchOrders(keyword, orderStatus, paymentStatus, deliveryMethod, pageable);
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }
    
    public Order updatePaymentStatus(Long orderId, Order.PaymentStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setPaymentStatus(status);
        if (status == Order.PaymentStatus.PAID) {
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        }
        return orderRepository.save(order);
    }
    
    public Order updateFastDeliveryStatus(Long orderId, String status, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        
        if (order.getDeliveryMethod() != Order.DeliveryMethod.FAST) {
            throw new RuntimeException("Đơn hàng này không phải giao hàng nhanh");
        }
        
        order.setFastDeliveryStatus(status);
        if (trackingNumber != null) {
            order.setTrackingNumber(trackingNumber);
        }
        return orderRepository.save(order);
    }
    
    public List<Order> getFastDeliveryOrders() {
        return orderRepository.findFastDeliveryOrders();
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
}

