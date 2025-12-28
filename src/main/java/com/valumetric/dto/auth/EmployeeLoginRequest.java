package com.valumetric.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 로그인 요청 DTO (MongoDB용 - String ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginRequest {

    @NotBlank(message = "사원 ID는 필수입니다")
    private String employeeId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
