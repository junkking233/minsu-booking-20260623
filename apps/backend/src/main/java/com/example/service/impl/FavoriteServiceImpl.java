package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.dto.FavoriteDetailDto;
import com.example.entity.Favorite;
import com.example.entity.House;
import com.example.exception.BusinessException;
import com.example.mapper.FavoriteMapper;
import com.example.mapper.HouseMapper;
import com.example.service.FavoriteService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final HouseMapper houseMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, HouseMapper houseMapper) {
        this.favoriteMapper = favoriteMapper;
        this.houseMapper = houseMapper;
    }

    @Override
    public void toggle(Long userId, Long houseId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("house_id", houseId);
        Favorite exist = favoriteMapper.selectOne(wrapper);
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
        } else {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setHouseId(houseId);
            favoriteMapper.insert(f);
        }
    }

    @Override
    public List<FavoriteDetailDto> myFavorites(Long userId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);
        
        List<FavoriteDetailDto> result = new ArrayList<>();
        for (Favorite fav : favorites) {
            House house = houseMapper.selectById(fav.getHouseId());
            result.add(FavoriteDetailDto.from(fav, house));
        }
        return result;
    }

    @Override
    public boolean isFavorite(Long userId, Long houseId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("house_id", houseId);
        return favoriteMapper.exists(wrapper);
    }
}
