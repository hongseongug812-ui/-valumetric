package com.valumetric.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * 급여 설정 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryConfigUpdateRequest {

    @NotNull(message = "1인당 고정비는 필수입니다")
    @Positive(message = "1인당 고정비는 양수여야 합니다")
    private BigDecimal fixedCostPerPerson;

    @NotNull(message = "4대보험료율은 필수입니다")
    private BigDecimal insuranceRate;

    @NotNull(message = "목표 이익률은 필수입니다")
    private BigDecimal targetProfitRate;
}
