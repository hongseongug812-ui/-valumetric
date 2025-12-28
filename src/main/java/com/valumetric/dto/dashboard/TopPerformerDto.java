package com.valumetric.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

/**
 * 우수 사원 정보 DTO
 * 
 * HCROI >= 1.5 또는 점수 >= 900 인 사원
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopPerformerDto {

    private String employeeId;
    private String employeeName;
    private String currentGrade;
    private BigDecimal currentSalary;

    private BigDecimal currentHcroi;
    private BigDecimal currentScore;
    private BigDecimal targetAchievementRate;

    private String performanceLevel; // EXCELLENT, OUTSTANDING
    private String achievement; // 성과 설명
    private int consecutiveMonths; // 연속 달성 개월 수
}
