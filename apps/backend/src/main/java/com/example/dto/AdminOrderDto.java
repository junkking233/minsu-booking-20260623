package com.example.dto;

import com.example.entity.House;
import com.example.entity.Order;
import java.math.BigDecimal;
import java.util.Map;

public class AdminOrderDto {
    private String orderNo;
    private Long houseId;
    private String houseName;
    private String houseCity;
    private Long userId;
    private String userName;
    private String guestName;
    private String phone;
    private String checkIn;
    private String checkOut;
    private Integer nights;
    private Integer guests;
    private BigDecimal amount;
    private String status;
    private String statusName;
    private boolean paid;
    private String note;
    private String createTime;

    private static final Map<String, String> STATUS_MAP = Map.ofEntries(
        Map.entry("PENDING", "待确认"),
        Map.entry("WAIT_PAY", "待支付"),
        Map.entry("BOOKED", "已预订"),
        Map.entry("CHECKED_IN", "入住中"),
        Map.entry("COMPLETED", "已完成"),
        Map.entry("REVIEWED", "已评价"),
        Map.entry("CANCELLED", "已取消"),
        Map.entry("REFUNDING", "退款中"),
        Map.entry("REFUNDED", "已退款")
    );

    public static AdminOrderDto from(Order order, House house, String userName) {
        AdminOrderDto dto = new AdminOrderDto();
        dto.orderNo = order.getOrderNo();
        dto.houseId = order.getHouseId();
        dto.userId = order.getUserId();
        dto.userName = userName;
        dto.guestName = order.getGuestName();
        dto.phone = order.getPhone();
        dto.checkIn = order.getCheckIn() != null ? order.getCheckIn().toString() : null;
        dto.checkOut = order.getCheckOut() != null ? order.getCheckOut().toString() : null;
        dto.nights = order.getNights();
        dto.guests = order.getGuests();
        dto.amount = order.getAmount();
        dto.status = order.getStatus();
        dto.statusName = STATUS_MAP.getOrDefault(order.getStatus(), order.getStatus());
        dto.paid = order.getPaid() != null && order.getPaid() == 1;
        dto.note = order.getNote();
        dto.createTime = order.getCreateTime() != null ? order.getCreateTime().toString() : null;
        if (house != null) {
            dto.houseName = house.getName();
            dto.houseCity = house.getCity();
        }
        return dto;
    }

    // Getters
    public String getOrderNo() { return orderNo; }
    public Long getHouseId() { return houseId; }
    public String getHouseName() { return houseName; }
    public String getHouseCity() { return houseCity; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getGuestName() { return guestName; }
    public String getPhone() { return phone; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public Integer getNights() { return nights; }
    public Integer getGuests() { return guests; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getStatusName() { return statusName; }
    public boolean isPaid() { return paid; }
    public String getNote() { return note; }
    public String getCreateTime() { return createTime; }
}
