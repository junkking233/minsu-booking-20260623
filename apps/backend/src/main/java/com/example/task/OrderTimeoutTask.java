package com.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.Order;
import com.example.entity.SystemConfig;
import com.example.mapper.OrderMapper;
import com.example.mapper.SystemConfigMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;

    private final SystemConfigMapper systemConfigMapper;

    public OrderTimeoutTask(OrderMapper orderMapper, SystemConfigMapper systemConfigMapper) {
        this.orderMapper = orderMapper;
        this.systemConfigMapper = systemConfigMapper;
    }

    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        int timeoutMinutes = getConfigInt("pay_timeout_minutes", 30);
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "WAIT_PAY")
                .lt("create_time", deadline);

        List<Order> timeoutOrders = orderMapper.selectList(wrapper);
        for (Order order : timeoutOrders) {
            Order update = new Order();
            update.setOrderNo(order.getOrderNo());
            update.setStatus("CANCELLED");
            orderMapper.updateById(update);
        }
    }

    private int getConfigInt(String key, int defaultValue) {
        SystemConfig config = systemConfigMapper.selectById(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
