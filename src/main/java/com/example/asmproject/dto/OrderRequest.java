package com.example.asmproject.dto;

import com.example.asmproject.model.enums.PaymentStatus;
import com.example.asmproject.model.enums.ShippingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    @NotNull(message = "Mã khách hàng không được để trống")
    private Long userId;

    private PaymentStatus paymentStatus = PaymentStatus.CHO_THANH_TOAN;

    private ShippingType shippingType = ShippingType.TIEU_CHUAN;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String note;

    @Valid
    @NotEmpty(message = "Đơn hàng cần ít nhất 1 sản phẩm")
    private List<OrderItemRequest> items = new ArrayList<>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public ShippingType getShippingType() {
        return shippingType;
    }

    public void setShippingType(ShippingType shippingType) {
        this.shippingType = shippingType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}

