package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    private String id;          // code ("exp-1")
    private String category;
    private String schoolId;    // school.code
    private String schoolName;
    private String city;
    private String title;
    private String excerpt;
    private String body;
    private Integer likes;      // likeCount
    private Integer comments;   // commentCount
    private List<String> tags;  // parsed from comma-separated string
}
