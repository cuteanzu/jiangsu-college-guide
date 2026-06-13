package com.jiangsu.guide.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {
    private Long schoolId;
    private String schoolName;

    @NotNull(message = "投稿类型不能为空")
    private String type;

    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private boolean isAnonymous;
    private String contact;
}
