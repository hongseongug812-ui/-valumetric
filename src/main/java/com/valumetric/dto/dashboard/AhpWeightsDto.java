package com.valumetric.dto.dashboard;

import lombok.*;
import java.util.List;

/**
 * AHP 가중치 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AhpWeightsDto {

    private List<CriteriaWeight> criteria;
    private Double consistencyRatio;
    private Boolean isConsistent;
    private String description;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CriteriaWeight {
        private String name; // "매출성과", "근태" 등
        private String description; // 설명
        private Double weight; // 가중치 (0.0 ~ 1.0)
        private Integer percentage; // 백분율 (0 ~ 100)
        private Integer displayOrder; // 표시 순서
    }
}
