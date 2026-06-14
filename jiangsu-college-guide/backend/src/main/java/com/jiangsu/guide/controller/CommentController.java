package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.common.SecurityUtils;
import com.jiangsu.guide.dto.CommentDTO;
import com.jiangsu.guide.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/schools/{schoolId}/comments")
    public Result<List<CommentDTO>> getComments(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(commentService.getCommentsBySchool(schoolId, page, size));
    }

    @PostMapping("/schools/{schoolId}/comments")
    public Result<CommentDTO> createComment(
            @PathVariable Long schoolId,
            @RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        String content = (String) body.get("content");
        String category = (String) body.getOrDefault("category", "GENERAL");
        boolean isAnonymous = (boolean) body.getOrDefault("isAnonymous", false);
        return Result.ok(commentService.createComment(userId, schoolId, content, category, isAnonymous));
    }

    @PostMapping("/comments/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.likeComment(userId, id);
        return Result.ok(null);
    }

    @DeleteMapping("/comments/{id}/like")
    public Result<Void> unlikeComment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.unlikeComment(userId, id);
        return Result.ok(null);
    }
}
