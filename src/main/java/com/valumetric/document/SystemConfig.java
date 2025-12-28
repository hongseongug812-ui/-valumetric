package com.valumetric.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 시스템 설정 Document (MongoDB)
 * 
 * <pre>
 * 단일 문서로 모든 설정 관리:
 * - 급여/비용 설정 (SalaryConfig)
 * - AHP 가중치 설정 (AhpConfig)
 * </pre>
 */
@Document(collection = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    private String id;

    @Builder.Default
    private String configType = "DEFAULT";

    // ==================== 급여/비용 설정 ====================

    private BigDecimal fixedCostPerPerson;

    private BigDecimal insuranceRate;

    private BigDecimal targetProfitRate;

    // ==================== AHP 설정 ====================

    /**
     * AHP 평가 기준 목록 (내장)
     */
    @Builder.Default
    private List<EvaluationCriteria> evaluationCriteria = new ArrayList<>();

    /**
     * AHP 쌍대비교 행렬 (상삼각 값)
     * 
     * 3x3 행렬 예시: [a12, a13, a23]
     */
    @Builder.Default
    private List<Double> ahpMatrixValues = new ArrayList<>();

    /**
     * 계산된 AHP 가중치
     */
    @Builder.Default
    private List<Double> ahpWeights = new ArrayList<>();

    private Double consistencyRatio;

    private Boolean isConsistent;

    private LocalDateTime updatedAt;

    // ==================== 내장 클래스 ====================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvaluationCriteria {
        private String name;
        private String description;
        private Double weight;
        @Builder.Default
        private Boolean isActive = true;
        private Integer displayOrder;
    }

    /**
     * 기본 설정 생성
     */
    public static SystemConfig createDefault() {
        List<EvaluationCriteria> defaultCriteria = List.of(
                EvaluationCriteria.builder()
                        .name("매출성과")
                        .description("HCROI 달성률 기반")
                        .weight(0.5)
                        .displayOrder(1)
                        .build(),
                EvaluationCriteria.builder()
                        .name("근태")
                        .description("출근률, 지각률 등")
                        .weight(0.3)
                        .displayOrder(2)
                        .build(),
                EvaluationCriteria.builder()
                        .name("기타성과")
                        .description("프로젝트 기여도 등")
                        .weight(0.2)
                        .displayOrder(3)
                        .build());

        return SystemConfig.builder()
                .configType("DEFAULT")
                .fixedCostPerPerson(new BigDecimal("500000"))
                .insuranceRate(new BigDecimal("0.0945"))
                .targetProfitRate(new BigDecimal("0.15"))
                .evaluationCriteria(new ArrayList<>(defaultCriteria))
                .ahpWeights(List.of(0.5, 0.3, 0.2))
                .consistencyRatio(0.0)
                .isConsistent(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
