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
public class CityProfileDTO {
    private Long id;
    private String name;
    private Integer schoolCount;
    private List<String> tags;
    private String cost;
    private String transit;
    private String jobs;
    private String audience;
}
