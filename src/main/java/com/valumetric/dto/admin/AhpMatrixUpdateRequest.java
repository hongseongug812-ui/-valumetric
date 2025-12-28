package com.valumetric.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * AHP 쌍대비교 행렬 수정 요청 DTO
 * 
 * <pre>
 * 【Saaty 척도 (1-9)】
 * 1: 동등 | 3: 약간 중요 | 5: 중요 | 7: 매우 중요 | 9: 절대 중요
 * 
 * 【행렬 예시 (3x3)】
 * 기준: 매출성과, 근태, 기타성과
 * 
 * 상삼각값만 입력 (대각선 제외):
 * - upperTriangleValues[0]: 매출 vs 근태 비교값
 * - upperTriangleValues[1]: 매출 vs 기타 비교값
 * - upperTriangleValues[2]: 근태 vs 기타 비교값
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AhpMatrixUpdateRequest {

    @NotNull(message = "행렬 크기는 필수입니다")
    private Integer matrixSize;

    @NotNull(message = "쌍대비교 값은 필수입니다")
    @Size(min = 1, message = "최소 1개의 쌍대비교 값이 필요합니다")
    private double[] upperTriangleValues;

    /**
     * 기준 이름 (선택, 디스플레이용)
     */
    private String[] criteriaNames;
}
