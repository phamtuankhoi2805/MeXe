package com.example.asmproject.service.mapper;

import com.example.asmproject.dto.OrderItemResponse;
import com.example.asmproject.dto.OrderResponse;
import com.example.asmproject.model.Order;
import com.example.asmproject.model.OrderItem;
import com.example.asmproject.model.enums.PaymentStatus;
import com.example.asmproject.model.enums.ShippingStatus;
import com.example.asmproject.model.enums.ShippingType;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setCode(order.getOrderCode());
        response.setUserId(order.getUser().getId());
        response.setUserName(order.getUser().getFullName());
        response.setTotalAmount(order.getTotal());
        response.setPaymentStatus(convertPaymentStatus(order.getPaymentStatus()));
        response.setShippingType(convertShippingType(order.getDeliveryMethod()));
        response.setShippingStatus(convertShippingStatus(order.getOrderStatus()));
        response.setNote(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(mapItems(order.getOrderItems()));
        return response;
    }

    private List<OrderItemResponse> mapItems(List<OrderItem> items) {
        return items.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductName(item.getProductName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getPrice());
            return itemResponse;
        }).collect(Collectors.toList());
    }

    private PaymentStatus convertPaymentStatus(Order.PaymentStatus status) {
        if (status == null) return null;
        switch (status) {
            case PENDING:
                return PaymentStatus.CHO_THANH_TOAN;
            case PAID:
                return PaymentStatus.DA_THANH_TOAN;
            case FAILED:
            case REFUNDED:
                return PaymentStatus.THAT_BAI;
            default:
                return PaymentStatus.CHO_THANH_TOAN;
        }
    }

    private ShippingType convertShippingType(Order.DeliveryMethod method) {
        if (method == null) return null;
        return method == Order.DeliveryMethod.FAST ? ShippingType.GIAO_NHANH : ShippingType.TIEU_CHUAN;
    }

    private ShippingStatus convertShippingStatus(Order.OrderStatus status) {
        if (status == null) return null;
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




