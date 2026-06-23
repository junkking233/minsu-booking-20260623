package com.example.dto;

import java.math.BigDecimal;

public class DashboardOverview {

    private Integer totalHouses;

    private Integer onShelfHouses;

    private Integer totalOrders;

    private Integer todayOrders;

    private BigDecimal todayRevenue;

    private Integer pendingOrders;

    private Integer totalUsers;

    private BigDecimal avgRating;

    public Integer getTotalHouses() {
        return totalHouses;
    }

    public void setTotalHouses(Integer totalHouses) {
        this.totalHouses = totalHouses;
    }

    public Integer getOnShelfHouses() {
        return onShelfHouses;
    }

    public void setOnShelfHouses(Integer onShelfHouses) {
        this.onShelfHouses = onShelfHouses;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(Integer todayOrders) {
        this.todayOrders = todayOrders;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public Integer getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Integer pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }
}
