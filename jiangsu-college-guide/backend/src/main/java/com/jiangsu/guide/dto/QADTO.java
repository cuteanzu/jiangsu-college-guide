package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QADTO {
    private String id;          // code ("qa-1")
    private String question;
    private String answer;
    private String schoolId;    // school.code
    private String schoolName;
    private String category;
    private Integer likes;
}
