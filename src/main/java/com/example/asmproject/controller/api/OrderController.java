package com.example.asmproject.controller.api;

import com.example.asmproject.model.Order;
import com.example.asmproject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long addressId = Long.valueOf(request.get("addressId").toString());
            String voucherCode = (String) request.get("voucherCode");
            String paymentMethod = (String) request.get("paymentMethod");
            String deliveryMethod = (String) request.getOrDefault("deliveryMethod", "STANDARD");
            
            Order.DeliveryMethod delivery = Order.DeliveryMethod.valueOf(deliveryMethod.toUpperCase());
            
            Order order = orderService.createOrder(userId, addressId, voucherCode, paymentMethod, delivery);
            response.put("success", true);
            response.put("message", "Đặt hàng thành công.");
            response.put("order", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/user/{userId}/page")
    public ResponseEntity<Page<Order>> getUserOrdersPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{orderCode}")
    public ResponseEntity<Order> getOrderByCode(@PathVariable String orderCode) {
        Optional<Order> order = orderService.getOrderByCode(orderCode);
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{orderId}/repurchase")
    public ResponseEntity<Map<String, Object>> repurchaseOrder(@PathVariable Long orderId,
                                                               @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.repurchaseOrder(userId, orderId);
            response.put("success", true);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng.");
            response.put("order", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

