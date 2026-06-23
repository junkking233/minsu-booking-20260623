package com.example.dto;

import java.util.List;

public class CalendarDto {
    private String houseName;
    private List<DayItem> days;

    public static class DayItem {
        private String date;
        private String status; // AVAILABLE, LOCKED, BOOKED
        private String orderNo;

        public DayItem(String date, String status) {
            this(date, status, null);
        }

        public DayItem(String date, String status, String orderNo) {
            this.date = date;
            this.status = status;
            this.orderNo = orderNo;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    }

    public String getHouseName() { return houseName; }
    public void setHouseName(String houseName) { this.houseName = houseName; }
    public List<DayItem> getDays() { return days; }
    public void setDays(List<DayItem> days) { this.days = days; }
}
