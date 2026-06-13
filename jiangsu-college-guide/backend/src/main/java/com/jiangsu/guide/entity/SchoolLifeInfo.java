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
@Table(name = "school_life_info")
public class SchoolLifeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "dorm_score")
    private Double dormScore;

    @Column(name = "dorm_desc", columnDefinition = "TEXT")
    private String dormDesc;

    @Column(name = "canteen_score")
    private Double canteenScore;

    @Column(name = "canteen_desc", columnDefinition = "TEXT")
    private String canteenDesc;

    @Column(name = "study_score")
    private Double studyScore;

    @Column(name = "study_desc", columnDefinition = "TEXT")
    private String studyDesc;

    @Column(name = "transport_score")
    private Double transportScore;

    @Column(name = "transport_desc", columnDefinition = "TEXT")
    private String transportDesc;

    @Column(name = "surrounding_score")
    private Double surroundingScore;

    @Column(name = "surrounding_desc", columnDefinition = "TEXT")
    private String surroundingDesc;

    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;

    @Column(name = "source_type", length = 20)
    @Builder.Default
    private String sourceType = "OFFICIAL";

    @Column(name = "audit_status", length = 20)
    @Builder.Default
    private String auditStatus = "APPROVED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
