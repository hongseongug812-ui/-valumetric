package com.valumetric.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

/**
 * 대시보드 요약 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {

    private Long totalEmployeeCount; // 전체 사원 수
    private BigDecimal averageHcroi; // 전체 사원 평균 HCROI
    private BigDecimal averageScore; // 전체 사원 평균 점수
    private Long redZoneCount; // 위험군 사원 수
    private Long unresolvedAlertCount; // 미해결 경고 수
    private BigDecimal companyTotalRevenue; // 회사 전체 매출
}
