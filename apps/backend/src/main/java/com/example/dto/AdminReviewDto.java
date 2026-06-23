package com.example.dto;

import com.example.entity.House;
import com.example.entity.Review;
import java.util.Map;

public class AdminReviewDto {
    private Long id;
    private String orderNo;
    private Long houseId;
    private String houseName;
    private Long userId;
    private String userName;
    private Integer rating;
    private Integer cleanScore;
    private Integer locationScore;
    private Integer serviceScore;
    private Integer valueScore;
    private String content;
    private String images;
    private boolean anonymous;
    private String status;
    private String reply;
    private String replyTime;
    private String createTime;

    public static AdminReviewDto from(Review review, String houseName) {
        AdminReviewDto dto = new AdminReviewDto();
        dto.id = review.getId();
        dto.orderNo = review.getOrderNo();
        dto.houseId = review.getHouseId();
        dto.houseName = houseName;
        dto.userId = review.getUserId();
        dto.userName = review.getUserName();
        dto.rating = review.getRating();
        dto.cleanScore = review.getCleanScore();
        dto.locationScore = review.getLocationScore();
        dto.serviceScore = review.getServiceScore();
        dto.valueScore = review.getValueScore();
        dto.content = review.getContent();
        dto.images = review.getImages();
        dto.anonymous = review.getAnonymous() != null && review.getAnonymous() == 1;
        dto.status = review.getStatus();
        dto.reply = review.getReply();
        dto.replyTime = review.getReplyTime() != null ? review.getReplyTime().toString() : null;
        dto.createTime = review.getCreateTime() != null ? review.getCreateTime().toString() : null;
        return dto;
    }

    // Getters
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public Long getHouseId() { return houseId; }
    public String getHouseName() { return houseName; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Integer getRating() { return rating; }
    public Integer getCleanScore() { return cleanScore; }
    public Integer getLocationScore() { return locationScore; }
    public Integer getServiceScore() { return serviceScore; }
    public Integer getValueScore() { return valueScore; }
    public String getContent() { return content; }
    public String getImages() { return images; }
    public boolean isAnonymous() { return anonymous; }
    public String getStatus() { return status; }
    public String getReply() { return reply; }
    public String getReplyTime() { return replyTime; }
    public String getCreateTime() { return createTime; }
}
