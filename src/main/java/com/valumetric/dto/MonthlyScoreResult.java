package com.valumetric.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 월별 점수 계산 결과 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyScoreResult {

    private Long employeeId;
    private String employeeName;
    private LocalDateTime calculatedAt;

    // HCROI 관련 지표
    private BigDecimal hcroiIndex; // HCROI 지수
    private BigDecimal targetAchievementRate; // 목표 달성률 (%)
    private BigDecimal breakEvenPointSales; // 손익분기점 매출액

    // 근태 점수
    private BigDecimal attendanceScore; // 근태 점수 (0-100)

    // AHP 가중치 적용 점수
    private Map<String, BigDecimal> weightedScores; // 기준별 가중 점수

    // 최종 점수
    private BigDecimal totalMonthlyScore; // 이번 달 획득 점수
    private String scoreGrade; // 등급 (S, A, B, C, D)

    // 점수 히스토리 ID
    private Long scoreHistoryId;
}
