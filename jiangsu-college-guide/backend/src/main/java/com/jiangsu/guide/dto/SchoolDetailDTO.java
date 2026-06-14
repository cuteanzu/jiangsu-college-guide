package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDetailDTO {
    private SchoolDTO basic;
    private LifeInfoDTO lifeInfo;
    private LifeSurveyDTO lifeSurvey;
    private long commentCount;
    private boolean isFavorited;
}
