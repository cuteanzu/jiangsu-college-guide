package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.CommentDTO;

import java.util.List;

public interface CommentService {
    CommentDTO createComment(Long userId, Long schoolId, String content, String category, boolean isAnonymous);
    List<CommentDTO> getCommentsBySchool(Long schoolId, int page, int size);
    void likeComment(Long userId, Long commentId);
    void unlikeComment(Long userId, Long commentId);
    boolean isLiked(Long userId, Long commentId);
    long getCommentCountBySchool(Long schoolId);
}
