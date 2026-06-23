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

    private TokenSubject currentUser(HttpServletRequest request) {
        Object subject = request.getAttribute("currentUser");
        if (subject instanceof TokenSubject tokenSubject) {
            return tokenSubject;
        }
        throw new BusinessException(401, "登录已失效，请重新登录");
    }
}
