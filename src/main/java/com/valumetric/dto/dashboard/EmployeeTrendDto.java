package com.valumetric.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 사원의 최근 6개월 추이 데이터 DTO (MongoDB용 - String ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTrendDto {

    private String employeeId;
    private String employeeName;
    private String currentGrade;

    private List<MonthlyTrendData> trendData;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTrendData {
        private String period;
        private BigDecimal revenue;
        private BigDecimal score;
        private BigDecimal hcroi;
        private BigDecimal achievementRate;
    }
}
