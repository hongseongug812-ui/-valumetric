package com.valumetric.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;

/**
 * 손익분기점(BEP) 달성 현황 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BepStatusDto {

    private String period; // 현재 기간 "2024-12"

    private BigDecimal targetRevenue; // 이번 달 목표 매출
    private BigDecimal currentRevenue; // 이번 달 현재 매출
    private BigDecimal bepRevenue; // 손익분기점 매출

    private BigDecimal achievementRate; // 목표 달성률 (%)
    private BigDecimal bepAchievementRate; // BEP 달성률 (%)

    private BigDecimal remainingToBep; // BEP까지 남은 금액 (음수면 초과 달성)
    private BigDecimal remainingToTarget; // 목표까지 남은 금액

    private boolean bepAchieved; // BEP 달성 여부
    private boolean targetAchieved; // 목표 달성 여부

    private int contributingEmployees; // 실적 기여 사원 수
}
