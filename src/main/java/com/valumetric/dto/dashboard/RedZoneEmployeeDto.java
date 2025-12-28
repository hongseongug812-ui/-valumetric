package com.valumetric.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

/**
 * 위험군(Red Zone) 사원 정보 DTO (MongoDB용 - String ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedZoneEmployeeDto {

    private String employeeId;
    private String employeeName;
    private String currentGrade;
    private BigDecimal currentSalary;

    private BigDecimal currentHcroi;
    private BigDecimal currentScore;
    private BigDecimal targetAchievementRate;

    private String riskLevel;
    private String riskReason;
    private Long unresolvedAlertCount;
}
