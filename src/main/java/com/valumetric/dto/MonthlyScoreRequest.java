package com.valumetric.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 월별 점수 계산을 위한 입력 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyScoreRequest {

    private Long employeeId;

    // 매출 관련
    private BigDecimal monthlyRevenue; // 이번 달 매출액

    // 근태 관련
    private Integer workDays; // 근무 일수
    private Integer lateCount; // 지각 횟수
    private Integer absenceCount; // 결근 횟수
    private Integer earlyLeaveCount; // 조퇴 횟수

    // 선택적: 추가 성과 지표
    private BigDecimal customerSatisfaction; // 고객 만족도 (0-100)
    private BigDecimal projectCompletionRate; // 프로젝트 완료율 (0-1)
}
