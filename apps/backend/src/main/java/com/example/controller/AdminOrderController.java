package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.dto.AdminOrderDto;
import com.example.dto.AdminOrderQuery;
import com.example.entity.Order;
import com.example.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<IPage<AdminOrderDto>> list(AdminOrderQuery query) {
        return Result.ok(orderService.adminList(query));
    }

    @PutMapping("/{orderNo}/confirm")
    public Result<Void> confirm(@PathVariable String orderNo) {
        orderService.confirm(orderNo);
        return Result.ok("确认成功", null);
    }

    @PutMapping("/{orderNo}/reject")
    public Result<Void> reject(@PathVariable String orderNo) {
        orderService.reject(orderNo);
        return Result.ok("拒绝成功", null);
    }

    @PutMapping("/{orderNo}/checkin")
    public Result<Void> checkIn(@PathVariable String orderNo) {
        orderService.checkIn(orderNo);
        return Result.ok("入住成功", null);
    }

    @PutMapping("/{orderNo}/complete")
    public Result<Void> complete(@PathVariable String orderNo) {
        orderService.complete(orderNo);
        return Result.ok("完成成功", null);
    }

    @PutMapping("/{orderNo}/refund")
    public Result<Void> startRefund(@PathVariable String orderNo) {
        orderService.startRefund(orderNo);
        return Result.ok("退款请求已提交", null);
    }

    @PutMapping("/{orderNo}/refund/approve")
    public Result<Void> approveRefund(@PathVariable String orderNo) {
        orderService.approveRefund(orderNo);
        return Result.ok("退款已同意", null);
    }

    @PutMapping("/{orderNo}/refund/reject")
    public Result<Void> rejectRefund(@PathVariable String orderNo) {
        orderService.rejectRefund(orderNo);
        return Result.ok("退款已拒绝", null);
    }
}
