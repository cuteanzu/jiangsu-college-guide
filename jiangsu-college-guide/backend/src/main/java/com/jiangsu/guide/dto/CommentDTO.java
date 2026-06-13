package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private Long schoolId;
    private Long userId;
    private String username;
    private String content;
    private String category;
    private boolean isAnonymous;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
