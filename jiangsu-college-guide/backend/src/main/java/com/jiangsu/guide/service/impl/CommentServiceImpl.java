package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.CommentDTO;
import com.jiangsu.guide.entity.Comment;
import com.jiangsu.guide.entity.User;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.CommentRepository;
import com.jiangsu.guide.repository.UserRepository;
import com.jiangsu.guide.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentDTO createComment(Long userId, Long schoolId, String content, String category, boolean isAnonymous) {
        log.info("▶ 发表评论: userId={}, schoolId={}, category={}, anonymous={}, content={}",
                userId, schoolId, category, isAnonymous, content.substring(0, Math.min(50, content.length())));
        Comment comment = Comment.builder()
                .userId(userId)
                .schoolId(schoolId)
                .content(content)
                .category(category != null ? category : "GENERAL")
                .isAnonymous(isAnonymous ? 1 : 0)
                .status("APPROVED")
                .likeCount(0)
                .build();

        comment = commentRepository.save(comment);
        log.info("✔ 评论发表成功: commentId={}", comment.getId());
        return toCommentDTO(comment);
    }

    @Override
    public List<CommentDTO> getCommentsBySchool(Long schoolId, int page, int size) {
        return commentRepository
                .findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "APPROVED", PageRequest.of(page, size))
                .stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void likeComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    @Override
    public long getCommentCountBySchool(Long schoolId) {
        return commentRepository.countBySchoolIdAndStatus(schoolId, "APPROVED");
    }

    private CommentDTO toCommentDTO(Comment c) {
        String displayName;
        if (c.getIsAnonymous() == 1) {
            displayName = "匿名用户";
        } else {
            User user = c.getUser();
            displayName = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知用户";
        }

        return CommentDTO.builder()
                .id(c.getId())
                .schoolId(c.getSchoolId())
                .userId(c.getUserId())
                .username(displayName)
                .content(c.getContent())
                .category(c.getCategory())
                .isAnonymous(c.getIsAnonymous() == 1)
                .likeCount(c.getLikeCount())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
