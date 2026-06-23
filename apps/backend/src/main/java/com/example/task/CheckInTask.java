package com.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.Order;
import com.example.mapper.OrderMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CheckInTask {

    private final OrderMapper orderMapper;

    public CheckInTask(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void autoCheckIn() {
        LocalDate today = LocalDate.now();

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "BOOKED")
                .eq("check_in", today);

        List<Order> orders = orderMapper.selectList(wrapper);
        for (Order order : orders) {
            Order update = new Order();
            update.setOrderNo(order.getOrderNo());
            update.setStatus("CHECKED_IN");
            orderMapper.updateById(update);
        }
    }
}
