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
@Table(name = "school")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private Double lat;

    @Column
    private Double lng;

    @Column
    private Integer founded;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", insertable = false, updatable = false)
    private City city;

    @Column(length = 20)
    private String type;

    @Column(length = 20)
    private String level;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(length = 200)
    private String website;

    @Column(length = 300)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String brief;

    @Column(name = "hot_score")
    @Builder.Default
    private Integer hotScore = 0;

    @Column(name = "favorite_count")
    @Builder.Default
    private Integer favoriteCount = 0;

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
