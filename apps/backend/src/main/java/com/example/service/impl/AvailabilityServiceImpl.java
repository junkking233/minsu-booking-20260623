package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.dto.CalendarDto;
import com.example.entity.Availability;
import com.example.entity.House;
import com.example.entity.Order;
import com.example.exception.BusinessException;
import com.example.mapper.AvailabilityMapper;
import com.example.mapper.HouseMapper;
import com.example.mapper.OrderMapper;
import com.example.service.AvailabilityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityMapper availabilityMapper;
    private final HouseMapper houseMapper;
    private final OrderMapper orderMapper;

    public AvailabilityServiceImpl(AvailabilityMapper availabilityMapper, 
                                   HouseMapper houseMapper, OrderMapper orderMapper) {
        this.availabilityMapper = availabilityMapper;
        this.houseMapper = houseMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public CalendarDto getCalendar(Long houseId, String yearMonth) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }

        // 解析年月
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 获取锁定日期
        QueryWrapper<Availability> availWrapper = new QueryWrapper<>();
        availWrapper.eq("house_id", houseId)
                .ge("date", startDate)
                .le("date", endDate);
        List<Availability> lockedDates = availabilityMapper.selectList(availWrapper);
        Set<String> lockedSet = new HashSet<>();
        for (Availability a : lockedDates) {
            lockedSet.add(a.getDate().toString());
        }

        // 获取已订日期（状态为 BOOKED 或 CHECKED_IN 的订单）
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("house_id", houseId)
                .in("status", "BOOKED", "CHECKED_IN")
                .le("check_in", endDate)
                .ge("check_out", startDate);
        List<Order> orders = orderMapper.selectList(orderWrapper);
        
        Set<String> bookedSet = new HashSet<>();
        for (Order order : orders) {
            LocalDate checkIn = order.getCheckIn();
            LocalDate checkOut = order.getCheckOut();
            LocalDate d = checkIn.isBefore(startDate) ? startDate : checkIn;
            LocalDate end = checkOut.isAfter(endDate) ? endDate : checkOut;
            while (!d.isAfter(end)) {
                bookedSet.add(d.toString());
                d = d.plusDays(1);
            }
        }

        // 生成每一天的状态
        List<CalendarDto.DayItem> days = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.toString();
            String status;
            if (bookedSet.contains(dateStr)) {
                status = "BOOKED";
            } else if (lockedSet.contains(dateStr)) {
                status = "LOCKED";
            } else {
                status = "AVAILABLE";
            }
            days.add(new CalendarDto.DayItem(dateStr, status));
            current = current.plusDays(1);
        }

        CalendarDto dto = new CalendarDto();
        dto.setHouseName(house.getName());
        dto.setDays(days);
        return dto;
    }

    @Override
    public void toggle(Long houseId, String date) {
        LocalDate d = LocalDate.parse(date);
        QueryWrapper<Availability> wrapper = new QueryWrapper<>();
        wrapper.eq("house_id", houseId).eq("date", d);
        Availability exist = availabilityMapper.selectOne(wrapper);
        if (exist != null) {
            availabilityMapper.deleteById(exist.getId());
        } else {
            Availability a = new Availability();
            a.setHouseId(houseId);
            a.setDate(d);
            availabilityMapper.insert(a);
        }
    }
}
