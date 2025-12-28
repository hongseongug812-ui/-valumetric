package com.valumetric.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;

/**
 * 잠재적 관리 대상(Watch List) 사원 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchListEmployeeDto {

    private String employeeId;
    private String employeeName;
    private String currentGrade;

    private BigDecimal currentHcroi;
    private BigDecimal currentScore;

    private BigDecimal previousHcroi; // 이전 월 HCROI
    private BigDecimal previousScore; // 이전 월 점수

    private BigDecimal hcroiChange; // HCROI 변화량
    private BigDecimal scoreChange; // 점수 변화량

    private String watchReason; // 관리 필요 사유
    private String riskLevel; // YELLOW, ORANGE

    private BigDecimal distanceToRedZone; // Red Zone까지 남은 거리
}
