package com.valumetric.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * 
 * <pre>
 * 【접근 URL】
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * 【JWT 인증】
 * - 상단 'Authorize' 버튼 클릭
 * - Bearer {token} 형식으로 입력
 * </pre>
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // API 기본 정보
                .info(apiInfo())

                // 서버 정보
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("개발 서버")))

                // JWT 보안 설정
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()));
    }

    /**
     * API 문서 기본 정보
     */
    private Info apiInfo() {
        return new Info()
                .title("ValuMetric API")
                .version("v1.0")
                .description("""
                        **HCROI 기반 인적자본 관리 시스템 API**

                        ### 주요 기능
                        - 📊 **Dashboard**: 평균 HCROI, 위험군 사원, 성과 추이
                        - 🔐 **Auth**: 사번/비밀번호 로그인, JWT 토큰 발급
                        - ⚙️ **Admin**: AHP 가중치, 급여 설정 관리

                        ### 인증 방법
                        1. `/api/auth/login` 으로 로그인
                        2. 응답의 `accessToken` 복사
                        3. 상단 **Authorize** 버튼 클릭
                        4. `Bearer {token}` 형식으로 입력
                        """)
                .contact(new Contact()
                        .name("ValuMetric Team")
                        .email("support@valumetric.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * JWT Bearer 인증 스키마
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT 토큰을 입력하세요. (Bearer 접두사는 자동 추가됨)");
    }
}
