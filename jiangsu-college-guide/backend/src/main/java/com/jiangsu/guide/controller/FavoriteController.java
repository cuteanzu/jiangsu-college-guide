package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.common.SecurityUtils;
import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Result<List<SchoolDTO>> getFavorites() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(favoriteService.getUserFavorites(userId));
    }

    @PostMapping("/{schoolId}")
    public Result<Void> addFavorite(@PathVariable Long schoolId) {
        Long userId = SecurityUtils.getCurrentUserId();
        favoriteService.addFavorite(userId, schoolId);
        return Result.ok("收藏成功", null);
    }

    @DeleteMapping("/{schoolId}")
    public Result<Void> removeFavorite(@PathVariable Long schoolId) {
        Long userId = SecurityUtils.getCurrentUserId();
        favoriteService.removeFavorite(userId, schoolId);
        return Result.ok("已取消收藏", null);
    }
}
