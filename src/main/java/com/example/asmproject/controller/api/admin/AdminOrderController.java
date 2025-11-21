package com.example.asmproject.controller.api.admin;

import com.example.asmproject.dto.OrderResponse;
import com.example.asmproject.model.Order;
import com.example.asmproject.model.enums.PaymentStatus;
import com.example.asmproject.model.enums.ShippingStatus;
import com.example.asmproject.model.enums.ShippingType;
import com.example.asmproject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý đơn hàng (Admin).
 * Xử lý các tác vụ tìm kiếm, xem chi tiết và cập nhật trạng thái đơn hàng.
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Tìm kiếm đơn hàng theo nhiều tiêu chí lọc.
     * Hỗ trợ lọc theo: từ khóa, loại giao hàng, trạng thái thanh toán/vận chuyển,
     * khoảng thời gian.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<OrderResponse>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ShippingType shippingType,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) ShippingStatus shippingStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.searchOrders(keyword, shippingType,
                paymentStatus, shippingStatus, fromDate, toDate, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Lấy chi tiết đơn hàng theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderResponseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cập nhật trạng thái xử lý của đơn hàng (Ví dụ: Duyệt, Đang giao, Đã giao...).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Convert string status sang enum
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            OrderResponse order = orderService.updateOrderStatus(id, orderStatus);

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

    /**
     * Cập nhật trạng thái thanh toán (Chưa thanh toán -> Đã thanh toán).
     */
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status.toUpperCase());
            OrderResponse order = orderService.updatePaymentStatus(id, paymentStatus);

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

    /**
     * Cập nhật thông tin giao hàng nhanh (Trạng thái vận chuyển của đơn vị vận
     * chuyển).
     */
    @PutMapping("/{id}/fast-delivery")
    public ResponseEntity<Map<String, Object>> updateFastDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String trackingNumber) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderResponse order = orderService.updateFastDeliveryStatus(id, status, trackingNumber);
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

    /**
     * Lấy danh sách các đơn hàng giao nhanh (API phụ trợ).
     */
    @GetMapping("/fast-delivery")
    public ResponseEntity<?> getFastDeliveryOrders() {
        return ResponseEntity.ok(orderService.getFastDeliveryOrders());
    }
}
