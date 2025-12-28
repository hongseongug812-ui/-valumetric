package com.valumetric.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 사원 등록 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCreateRequest {

    @NotBlank(message = "사원 이름은 필수입니다")
    private String name;

    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    private String currentGrade;

    @NotNull(message = "연봉은 필수입니다")
    @Positive(message = "연봉은 양수여야 합니다")
    private BigDecimal currentSalary;

    private LocalDate hireDate;

    private String role; // USER, ADMIN
}
