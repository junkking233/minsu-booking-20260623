package com.example.dto;

import com.example.entity.Order;
import java.math.BigDecimal;

public class OrderDetailDto {
    private String orderNo;
    private Long houseId;
    private String houseName;
    private String houseCoverImage;
    private String houseCity;
    private String houseAddress;
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

    private static final java.util.Map<String, String> STATUS_MAP = new java.util.HashMap<>();
    static {
        STATUS_MAP.put("PENDING", "待确认");
        STATUS_MAP.put("WAIT_PAY", "待支付");
        STATUS_MAP.put("BOOKED", "已预订");
        STATUS_MAP.put("CHECKED_IN", "入住中");
        STATUS_MAP.put("COMPLETED", "已完成");
        STATUS_MAP.put("REVIEWED", "已评价");
        STATUS_MAP.put("CANCELLED", "已取消");
        STATUS_MAP.put("REFUNDING", "退款中");
        STATUS_MAP.put("REFUNDED", "已退款");
    }

    public static OrderDetailDto from(Order order, com.example.entity.House house, com.example.entity.User user) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.orderNo = order.getOrderNo();
        dto.houseId = order.getHouseId();
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
            dto.houseCoverImage = house.getCoverImage();
            dto.houseCity = house.getCity();
            dto.houseAddress = house.getAddress();
        }
        if (user != null) {
            dto.userName = user.getName();
        }
        return dto;
    }

    // Getters
    public String getOrderNo() { return orderNo; }
    public Long getHouseId() { return houseId; }
    public String getHouseName() { return houseName; }
    public String getHouseCoverImage() { return houseCoverImage; }
    public String getHouseCity() { return houseCity; }
    public String getHouseAddress() { return houseAddress; }
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
