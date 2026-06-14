package com.jiangsu.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifeSurveyDTO {

    private String schoolName;
    private String sourceUrl;
    private Integer responseCount;

    private String dormSummary;
    private String acSummary;
    private String privateBathSummary;
    private String studyRuleSummary;
    private String runningSummary;
    private String vacationSummary;
    private String deliverySummary;
    private String transportSummary;
    private String laundrySummary;
    private String networkSummary;
    private String powerNetworkSummary;
    private String canteenSummary;
    private String hotWaterSummary;
    private String scooterSummary;
    private String powerLimitSummary;
    private String overnightStudySummary;
    private String computerSummary;
    private String paymentSummary;
    private String bankCardSummary;
    private String supermarketSummary;
    private String expressSummary;
    private String sharedBikeSummary;
    private String accessControlSummary;
}
