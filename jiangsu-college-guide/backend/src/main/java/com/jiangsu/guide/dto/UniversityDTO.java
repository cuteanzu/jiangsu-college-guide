package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDTO {
    private String id;       // school.code (e.g., "nju")
    private String name;
    private String city;
    private Double lat;
    private Double lng;
    private String tier;     // school.level (985/211/dual/provincial)
    private Integer founded;
    private String website;
}
