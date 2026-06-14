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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "school_life_survey_summary")
public class SchoolLifeSurveySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "school_name", nullable = false, length = 100)
    private String schoolName;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "response_count")
    private Integer responseCount;

    @Column(name = "dorm_summary", columnDefinition = "TEXT")
    private String dormSummary;

    @Column(name = "ac_summary", columnDefinition = "TEXT")
    private String acSummary;

    @Column(name = "private_bath_summary", columnDefinition = "TEXT")
    private String privateBathSummary;

    @Column(name = "study_rule_summary", columnDefinition = "TEXT")
    private String studyRuleSummary;

    @Column(name = "running_summary", columnDefinition = "TEXT")
    private String runningSummary;

    @Column(name = "vacation_summary", columnDefinition = "TEXT")
    private String vacationSummary;

    @Column(name = "delivery_summary", columnDefinition = "TEXT")
    private String deliverySummary;

    @Column(name = "transport_summary", columnDefinition = "TEXT")
    private String transportSummary;

    @Column(name = "laundry_summary", columnDefinition = "TEXT")
    private String laundrySummary;

    @Column(name = "network_summary", columnDefinition = "TEXT")
    private String networkSummary;

    @Column(name = "power_network_summary", columnDefinition = "TEXT")
    private String powerNetworkSummary;

    @Column(name = "canteen_summary", columnDefinition = "TEXT")
    private String canteenSummary;

    @Column(name = "hot_water_summary", columnDefinition = "TEXT")
    private String hotWaterSummary;

    @Column(name = "scooter_summary", columnDefinition = "TEXT")
    private String scooterSummary;

    @Column(name = "power_limit_summary", columnDefinition = "TEXT")
    private String powerLimitSummary;

    @Column(name = "overnight_study_summary", columnDefinition = "TEXT")
    private String overnightStudySummary;

    @Column(name = "computer_summary", columnDefinition = "TEXT")
    private String computerSummary;

    @Column(name = "payment_summary", columnDefinition = "TEXT")
    private String paymentSummary;

    @Column(name = "bank_card_summary", columnDefinition = "TEXT")
    private String bankCardSummary;

    @Column(name = "supermarket_summary", columnDefinition = "TEXT")
    private String supermarketSummary;

    @Column(name = "express_summary", columnDefinition = "TEXT")
    private String expressSummary;

    @Column(name = "shared_bike_summary", columnDefinition = "TEXT")
    private String sharedBikeSummary;

    @Column(name = "access_control_summary", columnDefinition = "TEXT")
    private String accessControlSummary;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
