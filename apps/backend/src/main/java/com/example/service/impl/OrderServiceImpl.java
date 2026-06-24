package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.AdminOrderDto;
import com.example.dto.AdminOrderQuery;
import com.example.dto.OrderCreateRequest;
import com.example.dto.OrderDetailDto;
import com.example.entity.House;
import com.example.entity.Order;
import com.example.entity.SystemConfig;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.HouseMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.SystemConfigMapper;
import com.example.mapper.UserMapper;
import com.example.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    private final HouseMapper houseMapper;

    private final UserMapper userMapper;

    private final SystemConfigMapper systemConfigMapper;

    public OrderServiceImpl(OrderMapper orderMapper, HouseMapper houseMapper,
                            UserMapper userMapper, SystemConfigMapper systemConfigMapper) {
        this.orderMapper = orderMapper;
        this.houseMapper = houseMapper;
        this.userMapper = userMapper;
        this.systemConfigMapper = systemConfigMapper;
    }

    @Override
    public Order create(Long userId, OrderCreateRequest request) {
        House house = houseMapper.selectById(request.getHouseId());
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }
        if (!"ON".equals(house.getStatus())) {
            throw new BusinessException(400, "该房源已下架");
        }

        LocalDate checkIn = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();

        if (!checkIn.isBefore(checkOut)) {
            throw new BusinessException(400, "入住日期必须早于离店日期");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessException(400, "入住日期不能早于今天");
        }

        if (request.getGuests() != null && request.getGuests() > house.getCapacity()) {
            throw new BusinessException(400, "入住人数超过房源最大容量");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        int discount = getConfigInt("default_discount", 30);
        BigDecimal amount = house.getPrice()
                .multiply(BigDecimal.valueOf(nights))
                .add(house.getCleanFee() != null ? house.getCleanFee() : BigDecimal.ZERO)
                .subtract(BigDecimal.valueOf(discount))
                .max(BigDecimal.ZERO);

        User user = userMapper.selectById(userId);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setHouseId(house.getId());
        order.setUserId(userId);
        order.setGuestName(request.getGuestName());
        order.setPhone(request.getPhone());
        order.setCheckIn(checkIn);
        order.setCheckOut(checkOut);
        order.setNights((int) nights);
        order.setGuests(request.getGuests() != null ? request.getGuests() : 1);
        order.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        order.setStatus("WAIT_PAY");
        order.setPaid(0);
        order.setNote(request.getNote());

        orderMapper.insert(order);
        return order;
    }

    @Override
    public IPage<OrderDetailDto> myOrders(Long userId, String status, Integer pageNum, Integer pageSize) {
        long pn = pageNum == null ? 1 : pageNum;
        long ps = pageSize == null ? 10 : pageSize;
        Page<Order> page = new Page<>(pn, ps);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);
        List<Order> records = orderPage.getRecords();
        Map<Long, House> loadedHouseMap = Collections.emptyMap();
        if (records != null && !records.isEmpty()) {
            List<Long> houseIds = records.stream()
                    .map(Order::getHouseId)
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
            if (!houseIds.isEmpty()) {
                loadedHouseMap = houseMapper.selectBatchIds(houseIds)
                        .stream()
                        .collect(Collectors.toMap(House::getId, Function.identity(), (a, b) -> a));
            }
        }

        Map<Long, House> houseMap = loadedHouseMap;
        Page<OrderDetailDto> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        dtoPage.setRecords(records == null ? Collections.emptyList() : records.stream()
                .map(order -> OrderDetailDto.from(order, houseMap.get(order.getHouseId()), null))
                .toList());
        return dtoPage;
    }

    @Override
    public OrderDetailDto getByOrderNo(Long userId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看该订单");
        }
        // 手机号脱敏
        maskPhone(order);
        
        House house = houseMapper.selectById(order.getHouseId());
        User user = userMapper.selectById(order.getUserId());
        return OrderDetailDto.from(order, house, user);
    }

    @Override
    public void cancel(Long userId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (!"PENDING".equals(order.getStatus())
                && !"WAIT_PAY".equals(order.getStatus())
                && !"BOOKED".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许取消");
        }
        if ("BOOKED".equals(order.getStatus())) {
            int freeCancelHours = getConfigInt("free_cancel_hours", 24);
            LocalDateTime freeCancelDeadline = order.getCheckIn().atStartOfDay().minusHours(freeCancelHours);
            if (LocalDateTime.now().isAfter(freeCancelDeadline)) {
                throw new BusinessException(400, "已超过免费取消时限，请联系管理员处理");
            }
        }

        Order update = new Order();
        update.setOrderNo(orderNo);
        update.setStatus("CANCELLED");
        // 取消订单时重置 paid 标记（已预订状态取消需回退 paid）
        if ("BOOKED".equals(order.getStatus())) {
            update.setPaid(0);
        }
        orderMapper.updateById(update);
    }

    @Override
    public void pay(Long userId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (!"WAIT_PAY".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许支付");
        }

        Order update = new Order();
        update.setOrderNo(orderNo);
        update.setStatus("BOOKED");
        update.setPaid(1);
        orderMapper.updateById(update);

        // 更新房源销量
        House house = houseMapper.selectById(order.getHouseId());
        if (house != null) {
            house.setSales(house.getSales() + 1);
            houseMapper.updateById(house);
        }
    }

    @Override
    public IPage<AdminOrderDto> adminList(AdminOrderQuery query) {
        long pageNum = query.getPage() == null ? 1 : query.getPage();
        long pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        Page<Order> page = new Page<>(pageNum, pageSize);
        IPage<Order> orderPage = orderMapper.searchOrders(page, query);
        
        // Convert to AdminOrderDto
        Page<AdminOrderDto> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(orderPage.getTotal());
        java.util.List<AdminOrderDto> dtoList = new java.util.ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            House house = houseMapper.selectById(order.getHouseId());
            User user = userMapper.selectById(order.getUserId());
            String userName = user != null ? user.getName() : null;
            dtoList.add(AdminOrderDto.from(order, house, userName));
        }
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public void confirm(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许确认");
        }
        updateOrderStatus(orderNo, "WAIT_PAY");
    }

    @Override
    public void reject(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许拒绝");
        }
        updateOrderStatus(orderNo, "CANCELLED");
    }

    @Override
    public void checkIn(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"BOOKED".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许入住");
        }
        updateOrderStatus(orderNo, "CHECKED_IN");
    }

    @Override
    public void complete(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"CHECKED_IN".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许完成");
        }
        updateOrderStatus(orderNo, "COMPLETED");
    }

    @Override
    public void startRefund(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"BOOKED".equals(order.getStatus()) && !"CHECKED_IN".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许退款");
        }
        updateOrderStatus(orderNo, "REFUNDING");
    }

    @Override
    public void approveRefund(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"REFUNDING".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许同意退款");
        }
        updateOrderStatus(orderNo, "REFUNDED");
    }

    @Override
    public void rejectRefund(String orderNo) {
        Order order = getOrder(orderNo);
        if (!"REFUNDING".equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许拒绝退款");
        }
        updateOrderStatus(orderNo, "BOOKED");
    }

    private Order getOrder(String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private void updateOrderStatus(String orderNo, String status) {
        Order update = new Order();
        update.setOrderNo(orderNo);
        update.setStatus(status);
        // 按需求同步 paid 标记：进入已预订/入住中/已完成/已评价 → paid=true；
        // 进入已取消/已退款 → paid=false
        switch (status) {
            case "BOOKED", "CHECKED_IN", "COMPLETED", "REVIEWED" -> update.setPaid(1);
            case "CANCELLED", "REFUNDED" -> update.setPaid(0);
        }
        orderMapper.updateById(update);
    }

    private String generateOrderNo() {
        // 使用时间戳+随机数生成唯一订单号，避免并发重复
        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = 100 + (int) (Math.random() * 900); // 100-999
        String orderNo = "MS" + timestamp + random;
        System.out.println("=== Generated orderNo: " + orderNo + " ===");
        return orderNo;
    }

    private void maskPhone(Order order) {
        if (order.getPhone() != null && order.getPhone().length() >= 7) {
            order.setPhone(order.getPhone().substring(0, 3) + "****" +
                    order.getPhone().substring(order.getPhone().length() - 4));
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
