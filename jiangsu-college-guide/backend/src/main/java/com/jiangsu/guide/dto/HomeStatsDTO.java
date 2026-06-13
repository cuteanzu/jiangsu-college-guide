package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatsDTO {
    private long cityCount;
    private long schoolCount;
    private long commentCount;
    private long submissionCount;
}
