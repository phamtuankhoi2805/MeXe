package com.example.asmproject.controller.api.admin;

import com.example.asmproject.model.Order;
import com.example.asmproject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/search")
    public ResponseEntity<Page<Order>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String deliveryMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Order.OrderStatus orderStatusEnum = null;
        if (orderStatus != null) {
            try {
                orderStatusEnum = Order.OrderStatus.valueOf(orderStatus.toUpperCase());
            } catch (IllegalArgumentException e) {}
        }
        
        Order.PaymentStatus paymentStatusEnum = null;
        if (paymentStatus != null) {
            try {
                paymentStatusEnum = Order.PaymentStatus.valueOf(paymentStatus.toUpperCase());
            } catch (IllegalArgumentException e) {}
        }
        
        Order.DeliveryMethod deliveryMethodEnum = null;
        if (deliveryMethod != null) {
            try {
                deliveryMethodEnum = Order.DeliveryMethod.valueOf(deliveryMethod.toUpperCase());
            } catch (IllegalArgumentException e) {}
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.searchOrders(keyword, orderStatusEnum, 
                                                       paymentStatusEnum, deliveryMethodEnum, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            Order order = orderService.updateOrderStatus(id, orderStatus);
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái đơn hàng thành công.");
            response.put("order", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status.toUpperCase());
            Order order = orderService.updatePaymentStatus(id, paymentStatus);
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thanh toán thành công.");
            response.put("order", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/fast-delivery")
    public ResponseEntity<Map<String, Object>> updateFastDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String trackingNumber) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.updateFastDeliveryStatus(id, status, trackingNumber);
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái giao hàng nhanh thành công.");
            response.put("order", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/fast-delivery")
    public ResponseEntity<?> getFastDeliveryOrders() {
        return ResponseEntity.ok(orderService.getFastDeliveryOrders());
    }
}

