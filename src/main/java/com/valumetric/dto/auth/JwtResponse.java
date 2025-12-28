package com.valumetric.dto.auth;

import lombok.*;

import java.util.Set;

/**
 * JWT 토큰 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;
    private String username;
    private Set<String> roles;
}
