package com.valumetric.dto.admin;

import lombok.*;

/**
 * AHP 가중치 계산 결과 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AhpWeightResponse {

    private double[] weights;
    private String[] criteriaNames;
    private double lambdaMax;
    private double consistencyIndex;
    private double consistencyRatio;
    private boolean isConsistent;
    private String message;
}
