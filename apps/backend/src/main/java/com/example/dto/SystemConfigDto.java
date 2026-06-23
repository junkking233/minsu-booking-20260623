package com.example.dto;

public class SystemConfigDto {

    private Integer defaultDiscount;

    private Integer payTimeoutMinutes;

    private Integer freeCancelHours;

    private Integer reviewAvailableDays;

    public Integer getDefaultDiscount() {
        return defaultDiscount;
    }

    public void setDefaultDiscount(Integer defaultDiscount) {
        this.defaultDiscount = defaultDiscount;
    }

    public Integer getPayTimeoutMinutes() {
        return payTimeoutMinutes;
    }

    public void setPayTimeoutMinutes(Integer payTimeoutMinutes) {
        this.payTimeoutMinutes = payTimeoutMinutes;
    }

    public Integer getFreeCancelHours() {
        return freeCancelHours;
    }

    public void setFreeCancelHours(Integer freeCancelHours) {
        this.freeCancelHours = freeCancelHours;
    }

    public Integer getReviewAvailableDays() {
        return reviewAvailableDays;
    }

    public void setReviewAvailableDays(Integer reviewAvailableDays) {
        this.reviewAvailableDays = reviewAvailableDays;
    }
}
