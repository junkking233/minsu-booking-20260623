package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dto.AdminOrderDto;
import com.example.dto.AdminOrderQuery;
import com.example.dto.OrderCreateRequest;
import com.example.dto.OrderDetailDto;
import com.example.entity.Order;

import java.util.Map;

public interface OrderService {

    Order create(Long userId, OrderCreateRequest request);

    IPage<Order> myOrders(Long userId, String status, Integer page, Integer pageSize);

    OrderDetailDto getByOrderNo(Long userId, String orderNo);

    void cancel(Long userId, String orderNo);

    void pay(Long userId, String orderNo);

    IPage<AdminOrderDto> adminList(AdminOrderQuery query);

    void confirm(String orderNo);

    void reject(String orderNo);

    void checkIn(String orderNo);

    void complete(String orderNo);

    void startRefund(String orderNo);

    void approveRefund(String orderNo);

    void rejectRefund(String orderNo);
}
