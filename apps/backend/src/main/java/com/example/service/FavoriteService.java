package com.example.service;

import com.example.dto.FavoriteDetailDto;
import com.example.entity.Favorite;

import java.util.List;

public interface FavoriteService {

    void toggle(Long userId, Long houseId);

    List<FavoriteDetailDto> myFavorites(Long userId);

    boolean isFavorite(Long userId, Long houseId);
}
