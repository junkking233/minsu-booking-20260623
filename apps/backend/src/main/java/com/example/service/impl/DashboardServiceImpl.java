package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.dto.DashboardOverview;
import com.example.dto.OrderTrendItem;
import com.example.entity.House;
import com.example.entity.Order;
import com.example.entity.User;
import com.example.mapper.HouseMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.FavoriteMapper;
import com.example.mapper.UserMapper;
import com.example.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final HouseMapper houseMapper;

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final FavoriteMapper favoriteMapper;

    public DashboardServiceImpl(HouseMapper houseMapper, OrderMapper orderMapper,
                                UserMapper userMapper, FavoriteMapper favoriteMapper) {
        this.houseMapper = houseMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public DashboardOverview overview() {
        DashboardOverview dto = new DashboardOverview();

        dto.setTotalHouses(Math.toIntExact(houseMapper.selectCount(null)));
        dto.setOnShelfHouses(Math.toIntExact(houseMapper.selectCount(
                new QueryWrapper<House>().eq("status", "ON"))));

        dto.setTotalOrders(Math.toIntExact(orderMapper.selectCount(null)));

        LocalDate today = LocalDate.now();
        QueryWrapper<Order> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("create_time", today.atStartOfDay())
                .lt("create_time", today.plusDays(1).atStartOfDay());
        dto.setTodayOrders(Math.toIntExact(orderMapper.selectCount(todayWrapper)));

        // Today revenue from paid orders
        BigDecimal todayRevenue = BigDecimal.ZERO;
        QueryWrapper<Order> paidWrapper = new QueryWrapper<>();
        paidWrapper.ge("create_time", today.atStartOfDay())
                .lt("create_time", today.plusDays(1).atStartOfDay())
                .eq("paid", 1);
        List<Order> todayOrders = orderMapper.selectList(paidWrapper);
        for (Order o : todayOrders) {
            if (o.getAmount() != null) {
                todayRevenue = todayRevenue.add(o.getAmount());
            }
        }
        dto.setTodayRevenue(todayRevenue);

        // Pending orders
        QueryWrapper<Order> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.in("status", "PENDING", "WAIT_PAY");
        dto.setPendingOrders(Math.toIntExact(orderMapper.selectCount(pendingWrapper)));

        dto.setTotalUsers(Math.toIntExact(userMapper.selectCount(null)));

        // Average rating
        BigDecimal avgRating = BigDecimal.ZERO;
        try {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<House> avgWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            avgWrapper.select("COALESCE(AVG(rating), 5.0) as avgRating");
            avgWrapper.eq("status", "ON");
            java.util.Map<String, Object> result = houseMapper.selectMaps(avgWrapper).get(0);
            if (result.get("avgRating") != null) {
                avgRating = new BigDecimal(result.get("avgRating").toString());
            }
        } catch (Exception e) {
            avgRating = new BigDecimal("5.0");
        }
        dto.setAvgRating(avgRating);

        // 收藏次数
        dto.setTotalFavorites(Math.toIntExact(favoriteMapper.selectCount(null)));

        return dto;
    }

    @Override
    public List<OrderTrendItem> orderTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        List<OrderTrendItem> trends = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.ge("create_time", date.atStartOfDay())
                    .lt("create_time", date.plusDays(1).atStartOfDay());
            long count = orderMapper.selectCount(wrapper);
            trends.add(new OrderTrendItem(date.format(DateTimeFormatter.ISO_LOCAL_DATE), Math.toIntExact(count)));
        }
        return trends;
    }

    @Override
    public List<Order> pendingOrders() {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.in("status", "PENDING", "WAIT_PAY")
                .orderByAsc("create_time")
                .last("LIMIT 20");
        return orderMapper.selectList(wrapper);
    }
}
