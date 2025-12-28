package com.valumetric.dto.auth;

import lombok.*;

/**
 * 토큰 응답 DTO (MongoDB용 - String ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private String employeeId;
    private String employeeName;
    private String role;
}
