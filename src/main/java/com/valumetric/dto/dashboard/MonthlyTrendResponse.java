package com.valumetric.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 월별 추이 데이터 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTrendResponse {

    private List<MonthlyData> data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyData {
        private String period; // "2024-07"
        private BigDecimal totalRevenue; // 월별 총 매출
        private BigDecimal totalLaborCost; // 월별 총 인건비
        private BigDecimal averageHcroi; // 월별 평균 HCROI
        private int employeeCount; // 해당 월 사원 수
    }
}
