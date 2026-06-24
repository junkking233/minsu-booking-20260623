package com.example.controller;

import com.example.common.Result;
import com.example.dto.ReviewCreateRequest;
import com.example.entity.Review;
import com.example.exception.BusinessException;
import com.example.service.ReviewService;
import com.example.util.TokenSubject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Result<Review> create(@RequestBody ReviewCreateRequest request, HttpServletRequest httpRequest) {
        return Result.ok("评价成功", reviewService.create(currentUser(httpRequest).getUserId(), request));
    }

    /** 用户端：查询指定房源的可见评价列表 */
    @GetMapping("/house/{houseId}")
    public Result<java.util.List<Review>> listByHouse(@PathVariable Long houseId) {
        return Result.ok(reviewService.listByHouse(houseId));
    }

    private TokenSubject currentUser(HttpServletRequest request) {
        Object subject = request.getAttribute("currentUser");
        if (subject instanceof TokenSubject tokenSubject) {
            return tokenSubject;
        }
        throw new BusinessException(401, "登录已失效，请重新登录");
    }
}
