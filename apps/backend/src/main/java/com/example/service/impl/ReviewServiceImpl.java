package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.AdminReviewDto;
import com.example.dto.ReviewCreateRequest;
import com.example.dto.ReviewQuery;
import com.example.entity.House;
import com.example.entity.Order;
import com.example.entity.Review;
import com.example.entity.SystemConfig;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.HouseMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.ReviewMapper;
import com.example.mapper.SystemConfigMapper;
import com.example.mapper.UserMapper;
import com.example.service.ReviewService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;

    private final OrderMapper orderMapper;

    private final HouseMapper houseMapper;

    private final UserMapper userMapper;

    private final SystemConfigMapper systemConfigMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper, OrderMapper orderMapper,
                             HouseMapper houseMapper, UserMapper userMapper,
                             SystemConfigMapper systemConfigMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.houseMapper = houseMapper;
        this.userMapper = userMapper;
        this.systemConfigMapper = systemConfigMapper;
    }

    @Override
    public Review create(Long userId, ReviewCreateRequest request) {
        Order order = orderMapper.selectById(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权评价该订单");
        }
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException(400, "订单未完成，无法评价");
        }
        int reviewAvailableDays = getConfigInt("review_available_days", 30);
        if (order.getCheckOut() != null && java.time.LocalDate.now().isAfter(order.getCheckOut().plusDays(reviewAvailableDays))) {
            throw new BusinessException(400, "评价入口已过期");
        }

        // Check if already reviewed
        QueryWrapper<Review> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("order_no", request.getOrderNo());
        if (reviewMapper.exists(checkWrapper)) {
            throw new BusinessException(409, "该订单已评价");
        }

        User user = userMapper.selectById(userId);

        Review review = new Review();
        review.setOrderNo(request.getOrderNo());
        review.setHouseId(order.getHouseId());
        review.setUserId(userId);
        if (request.getAnonymous() != null && request.getAnonymous() == 1) {
            review.setUserName("匿名用户");
        } else {
            review.setUserName(user.getName() != null ? user.getName() : user.getUsername());
        }
        review.setRating(request.getRating());
        review.setCleanScore(request.getCleanScore());
        review.setLocationScore(request.getLocationScore());
        review.setServiceScore(request.getServiceScore());
        review.setValueScore(request.getValueScore());
        review.setContent(request.getContent());
        review.setImages(request.getImages() != null ? String.join(",", request.getImages()) : null);
        review.setAnonymous(request.getAnonymous() != null ? request.getAnonymous() : 0);
        review.setStatus("VISIBLE");

        reviewMapper.insert(review);

        // Update order status
        Order orderUpdate = new Order();
        orderUpdate.setOrderNo(request.getOrderNo());
        orderUpdate.setStatus("REVIEWED");
        orderMapper.updateById(orderUpdate);

        // Recalculate house rating
        recalculateHouseRating(order.getHouseId());

        return review;
    }

    @Override
    public IPage<AdminReviewDto> adminList(ReviewQuery query) {
        long pageNum = query.getPage() == null ? 1 : query.getPage();
        long pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        Page<Review> page = new Page<>(pageNum, pageSize);
        IPage<Review> reviewPage = reviewMapper.searchReviews(page, query);
        
        // Convert to AdminReviewDto
        Page<AdminReviewDto> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(reviewPage.getTotal());
        List<AdminReviewDto> dtoList = new ArrayList<>();
        for (Review review : reviewPage.getRecords()) {
            House house = houseMapper.selectById(review.getHouseId());
            String houseName = house != null ? house.getName() : null;
            dtoList.add(AdminReviewDto.from(review, houseName));
        }
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public void reply(Long reviewId, String reply) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }

        Review update = new Review();
        update.setId(reviewId);
        update.setReply(reply);
        update.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(update);
    }

    @Override
    public void updateStatus(Long reviewId, String status) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }

        Review update = new Review();
        update.setId(reviewId);
        update.setStatus(status);
        reviewMapper.updateById(update);

        // Recalculate house rating when hiding/showing review
        recalculateHouseRating(review.getHouseId());
    }

    @Override
    public void delete(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }

        Long houseId = review.getHouseId();
        reviewMapper.deleteById(reviewId);

        // Recalculate house rating
        recalculateHouseRating(houseId);
    }

    private void recalculateHouseRating(Long houseId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("house_id", houseId)
                .eq("status", "VISIBLE");
        List<Review> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            com.example.entity.House house = houseMapper.selectById(houseId);
            if (house != null) {
                house.setRating(new BigDecimal("5.0"));
                houseMapper.updateById(house);
            }
        } else {
            double avg = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(5.0);
            com.example.entity.House house = houseMapper.selectById(houseId);
            if (house != null) {
                house.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
                houseMapper.updateById(house);
            }
        }
    }

    @Override
    public List<Review> listByHouse(Long houseId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("house_id", houseId)
                .eq("status", "VISIBLE")
                .orderByDesc("create_time");
        return reviewMapper.selectList(wrapper);
    }

    private int getConfigInt(String key, int defaultValue) {
        SystemConfig config = systemConfigMapper.selectById(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
