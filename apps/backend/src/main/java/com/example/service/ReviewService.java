package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dto.AdminReviewDto;
import com.example.dto.ReviewCreateRequest;
import com.example.dto.ReviewQuery;
import com.example.entity.Review;

public interface ReviewService {

    Review create(Long userId, ReviewCreateRequest request);

    IPage<AdminReviewDto> adminList(ReviewQuery query);

    void reply(Long reviewId, String reply);

    void updateStatus(Long reviewId, String status);

    void delete(Long reviewId);
}
