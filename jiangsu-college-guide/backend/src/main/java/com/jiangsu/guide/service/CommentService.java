package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.CommentDTO;

import java.util.List;

public interface CommentService {
    CommentDTO createComment(Long userId, Long schoolId, String content, String category, boolean isAnonymous);
    List<CommentDTO> getCommentsBySchool(Long schoolId, int page, int size);
    void likeComment(Long commentId);
    long getCommentCountBySchool(Long schoolId);
}
