package com.example.controller;

import com.example.common.Result;
import com.example.dto.DashboardOverview;
import com.example.dto.OrderTrendItem;
import com.example.entity.Order;
import com.example.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Result<DashboardOverview> overview() {
        return Result.ok(dashboardService.overview());
    }

    @GetMapping("/order-trend")
    public Result<List<OrderTrendItem>> orderTrend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.ok(dashboardService.orderTrend(days));
    }

    @GetMapping("/pending-orders")
    public Result<List<Order>> pendingOrders() {
        return Result.ok(dashboardService.pendingOrders());
    }
}
