package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.dto.OrderCreateRequest;
import com.example.dto.OrderDetailDto;
import com.example.entity.Order;
import com.example.exception.BusinessException;
import com.example.service.OrderService;
import com.example.util.TokenSubject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<Order> create(@RequestBody OrderCreateRequest request, HttpServletRequest httpRequest) {
        return Result.ok("下单成功", orderService.create(currentUser(httpRequest).getUserId(), request));
    }

    @GetMapping("/my")
    public Result<IPage<OrderDetailDto>> myOrders(@RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest httpRequest) {
        return Result.ok(orderService.myOrders(currentUser(httpRequest).getUserId(), status, page, pageSize));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDetailDto> detail(@PathVariable String orderNo, HttpServletRequest httpRequest) {
        return Result.ok(orderService.getByOrderNo(currentUser(httpRequest).getUserId(), orderNo));
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo, HttpServletRequest httpRequest) {
        orderService.cancel(currentUser(httpRequest).getUserId(), orderNo);
        return Result.ok("取消成功", null);
    }

    @PutMapping("/{orderNo}/pay")
    public Result<Void> pay(@PathVariable String orderNo, HttpServletRequest httpRequest) {
        orderService.pay(currentUser(httpRequest).getUserId(), orderNo);
        return Result.ok("支付成功", null);
    }

    private TokenSubject currentUser(HttpServletRequest request) {
        Object subject = request.getAttribute("currentUser");
        if (subject instanceof TokenSubject tokenSubject) {
            return tokenSubject;
        }
        throw new BusinessException(401, "登录已失效，请重新登录");
    }
}
