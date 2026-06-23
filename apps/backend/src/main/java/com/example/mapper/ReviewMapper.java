package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.ReviewQuery;
import com.example.entity.Review;
import org.apache.ibatis.annotations.Param;

public interface ReviewMapper extends BaseMapper<Review> {

    IPage<Review> searchReviews(Page<Review> page, @Param("query") ReviewQuery query);
}
