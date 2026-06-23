package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.HouseSearchQuery;
import com.example.entity.House;
import org.apache.ibatis.annotations.Param;

public interface HouseMapper extends BaseMapper<House> {

    IPage<House> searchHouses(Page<House> page, @Param("query") HouseSearchQuery query);
}
