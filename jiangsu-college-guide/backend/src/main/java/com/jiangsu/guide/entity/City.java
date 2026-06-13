package com.jiangsu.guide.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(length = 100)
    private String pinyin;

    @Column(length = 50)
    @Builder.Default
    private String province = "江苏省";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "school_count")
    @Builder.Default
    private Integer schoolCount = 0;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(length = 300)
    private String tags;

    @Column(name = "cost_guide", length = 50)
    private String costGuide;

    @Column(name = "transit_guide", length = 50)
    private String transitGuide;

    @Column(name = "jobs_guide", length = 50)
    private String jobsGuide;

    @Column(columnDefinition = "TEXT")
    private String audience;

    @Column(name = "map_x")
    private Double mapX;

    @Column(name = "map_y")
    private Double mapY;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
