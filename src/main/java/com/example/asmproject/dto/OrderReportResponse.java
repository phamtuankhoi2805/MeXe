package com.example.asmproject.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderReportResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long fastShippingOrders;
    private long standardShippingOrders;

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getFastShippingOrders() {
        return fastShippingOrders;
    }

    public void setFastShippingOrders(long fastShippingOrders) {
        this.fastShippingOrders = fastShippingOrders;
    }

    public long getStandardShippingOrders() {
        return standardShippingOrders;
    }

    public void setStandardShippingOrders(long standardShippingOrders) {
        this.standardShippingOrders = standardShippingOrders;
    }
}




