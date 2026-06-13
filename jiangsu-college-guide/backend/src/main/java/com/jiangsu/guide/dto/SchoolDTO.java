package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDTO {
    private Long id;
    private String name;
    private Long cityId;
    private String cityName;
    private String type;
    private String level;
    private String logoUrl;
    private String coverUrl;
    private String website;
    private String address;
    private String brief;
    private Integer hotScore;
    private Integer favoriteCount;
    private Double mapX;
    private Double mapY;
    private Boolean isFavorited;
}
