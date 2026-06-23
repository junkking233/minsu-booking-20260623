package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.dto.AdminReviewDto;
import com.example.dto.ReplyRequest;
import com.example.dto.ReviewQuery;
import com.example.entity.Review;
import com.example.service.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public Result<IPage<AdminReviewDto>> list(ReviewQuery query) {
        return Result.ok(reviewService.adminList(query));
    }

    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody ReplyRequest request) {
        reviewService.reply(id, request.getReply());
        return Result.ok("回复成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        reviewService.updateStatus(id, body.get("status"));
        return Result.ok("操作成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.ok("删除成功", null);
    }
}
