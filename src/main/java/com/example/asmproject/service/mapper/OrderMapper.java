package com.example.asmproject.service.mapper;

import com.example.asmproject.dto.OrderItemResponse;
import com.example.asmproject.dto.OrderResponse;
import com.example.asmproject.model.Order;
import com.example.asmproject.model.OrderItem;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setCode(order.getCode());
        response.setUserId(order.getUser().getId());
        response.setUserName(order.getUser().getFullName());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setShippingType(order.getShippingType());
        response.setShippingStatus(order.getShippingStatus());
        response.setNote(order.getNote());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(mapItems(order.getItems()));
        return response;
    }

    private List<OrderItemResponse> mapItems(List<OrderItem> items) {
        return items.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductName(item.getProductName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            return itemResponse;
        }).collect(Collectors.toList());
    }
}




