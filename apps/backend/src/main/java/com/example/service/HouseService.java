package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dto.HouseSaveRequest;
import com.example.dto.HouseSearchQuery;
import com.example.entity.House;

import java.util.Map;

public interface HouseService {

    IPage<House> search(HouseSearchQuery query);

    House getById(Long id);

    Map<String, Object> checkAvailability(Long houseId, String checkIn, String checkOut);

    IPage<House> adminList(Integer page, Integer pageSize);

    House create(HouseSaveRequest request);

    House update(Long id, HouseSaveRequest request);

    void updateStatus(Long id, String status);

    void delete(Long id);
}
