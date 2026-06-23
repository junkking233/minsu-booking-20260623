package com.example.dto;

import com.example.entity.Favorite;
import com.example.entity.House;
import java.math.BigDecimal;

public class FavoriteDetailDto {
    private Long id;
    private Long houseId;
    private String houseName;
    private String houseCoverImage;
    private String houseCity;
    private BigDecimal housePrice;
    private BigDecimal houseRating;
    private String houseStatus;
    private String createTime;

    public static FavoriteDetailDto from(Favorite fav, House house) {
        FavoriteDetailDto dto = new FavoriteDetailDto();
        dto.id = fav.getId();
        dto.houseId = fav.getHouseId();
        dto.createTime = fav.getCreateTime() != null ? fav.getCreateTime().toString() : null;

        if (house != null) {
            dto.houseName = house.getName();
            dto.houseCoverImage = house.getCoverImage();
            dto.houseCity = house.getCity();
            dto.housePrice = house.getPrice();
            dto.houseRating = house.getRating();
            dto.houseStatus = house.getStatus();
        }
        return dto;
    }

    // Getters
    public Long getId() { return id; }
    public Long getHouseId() { return houseId; }
    public String getHouseName() { return houseName; }
    public String getHouseCoverImage() { return houseCoverImage; }
    public String getHouseCity() { return houseCity; }
    public BigDecimal getHousePrice() { return housePrice; }
    public BigDecimal getHouseRating() { return houseRating; }
    public String getHouseStatus() { return houseStatus; }
    public String getCreateTime() { return createTime; }
}
