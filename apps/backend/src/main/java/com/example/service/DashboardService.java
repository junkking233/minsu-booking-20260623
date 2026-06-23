package com.example.service;

import com.example.dto.DashboardOverview;
import com.example.dto.OrderTrendItem;
import com.example.entity.Order;

import java.util.List;

public interface DashboardService {

    DashboardOverview overview();

    List<OrderTrendItem> orderTrend(Integer days);

    List<Order> pendingOrders();
}
