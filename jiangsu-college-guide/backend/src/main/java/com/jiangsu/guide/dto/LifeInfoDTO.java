package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifeInfoDTO {
    private Double dormScore;
    private String dormDesc;
    private Double canteenScore;
    private String canteenDesc;
    private Double studyScore;
    private String studyDesc;
    private Double transportScore;
    private String transportDesc;
    private Double surroundingScore;
    private String surroundingDesc;
    private String tips;
    private String sourceType;
}
