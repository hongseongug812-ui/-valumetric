package com.valumetric.dto.employee;

import lombok.*;

import java.math.BigDecimal;

/**
 * 월별 실적 입력 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceLogRequest {

    private String employeeId;

    private String period; // "2024-01" 형식

    private BigDecimal targetSales; // 목표 매출

    private BigDecimal achievedSales; // 달성 매출

    private BigDecimal profit; // 이익
}
