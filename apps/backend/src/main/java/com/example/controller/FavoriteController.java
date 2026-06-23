package com.example.controller;

import com.example.common.Result;
import com.example.dto.FavoriteDetailDto;
import com.example.entity.Favorite;
import com.example.exception.BusinessException;
import com.example.service.FavoriteService;
import com.example.util.TokenSubject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{houseId}")
    public Result<Void> toggle(@PathVariable Long houseId, HttpServletRequest httpRequest) {
        favoriteService.toggle(currentUser(httpRequest).getUserId(), houseId);
        return Result.ok(null);
    }

    @DeleteMapping("/{houseId}")
    public Result<Void> remove(@PathVariable Long houseId, HttpServletRequest httpRequest) {
        favoriteService.toggle(currentUser(httpRequest).getUserId(), houseId);
        return Result.ok(null);
    }

    @GetMapping("/my")
    public Result<List<FavoriteDetailDto>> myFavorites(HttpServletRequest httpRequest) {
        return Result.ok(favoriteService.myFavorites(currentUser(httpRequest).getUserId()));
    }

    @GetMapping("/check/{houseId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long houseId, HttpServletRequest httpRequest) {
        boolean favorited = favoriteService.isFavorite(currentUser(httpRequest).getUserId(), houseId);
        return Result.ok(Map.of("favorited", favorited));
    }

    private TokenSubject currentUser(HttpServletRequest request) {
        Object subject = request.getAttribute("currentUser");
        if (subject instanceof TokenSubject tokenSubject) {
            return tokenSubject;
        }
        throw new BusinessException(401, "登录已失效，请重新登录");
    }
}
