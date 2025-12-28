package com.valumetric.dto.employee;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 사원 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private String id;
    private String name;
    private String email;
    private String currentGrade;
    private BigDecimal currentSalary;
    private LocalDate hireDate;
    private String role;
    private Boolean isEnabled;

    private int performanceLogCount;
    private int alertCount;
}
