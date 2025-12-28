package com.valumetric.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 회원가입 요청 DTO
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다")
    private String password;

    private String companyName;

    private String currentGrade = "사원";

    private BigDecimal currentSalary = new BigDecimal("36000000"); // 기본 연봉 3600만
}
