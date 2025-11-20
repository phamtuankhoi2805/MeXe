package com.example.asmproject.controller.api;

import com.example.asmproject.dto.OrderRequest;
import com.example.asmproject.dto.OrderResponse;
import com.example.asmproject.dto.OrderReportResponse;
import com.example.asmproject.dto.ShippingStatusResponse;
import com.example.asmproject.model.enums.PaymentStatus;
import com.example.asmproject.model.enums.ShippingStatus;
import com.example.asmproject.model.enums.ShippingType;
import com.example.asmproject.service.OrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderResponse> timKiemDonHang(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) ShippingType shippingType,
                                              @RequestParam(required = false) PaymentStatus paymentStatus,
                                              @RequestParam(required = false) ShippingStatus shippingStatus,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                              LocalDate fromDate,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                              LocalDate toDate,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return orderService.searchOrders(
            keyword, shippingType, paymentStatus, shippingStatus, fromDate, toDate, PageRequest.of(page, size));
    }

    @PostMapping
    public OrderResponse taoDonHang(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{code}/shipping-status")
    public ShippingStatusResponse xemTrangThaiGiaoNhanh(@PathVariable String code) {
        return orderService.getShippingStatus(code);
    }

    @GetMapping("/report")
    public ResponseEntity<?> xuatBaoCaoDonHang(@RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                               LocalDate fromDate,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                               LocalDate toDate,
                                               @RequestParam(defaultValue = "json") String format) {
        OrderReportResponse report = orderService.buildReport(fromDate, toDate);
        if ("csv".equalsIgnoreCase(format)) {
            String csv = orderService.buildReportCsv(fromDate, toDate);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao-cao-don-hang.csv")
                .contentType(Objects.requireNonNull(MediaType.TEXT_PLAIN))
                .body(csv);
        }
        return ResponseEntity.ok(report);
    }
}

