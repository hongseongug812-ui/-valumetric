package com.valumetric.controller;

import com.valumetric.document.Employee;
import com.valumetric.dto.auth.EmployeeLoginRequest;
import com.valumetric.dto.auth.TokenResponse;
import com.valumetric.repository.EmployeeRepository;
import com.valumetric.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 인증 REST API 컨트롤러 (MongoDB 버전)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증 API - 로그인, 토큰 갱신")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final EmployeeRepository employeeRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        @Operation(summary = "로그인", description = "사원 ID 또는 이메일과 비밀번호로 로그인")
        @PostMapping("/login")
        public ResponseEntity<?> login(@Valid @RequestBody EmployeeLoginRequest request) {
                log.info("로그인 시도: {}", request.getEmployeeId());

                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmployeeId(),
                                                        request.getPassword()));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

                        // 마지막 로그인 시간 업데이트
                        Employee employee = employeeRepository.findById(request.getEmployeeId())
                                        .or(() -> employeeRepository.findByEmail(request.getEmployeeId()))
                                        .orElseThrow(() -> new RuntimeException("사원을 찾을 수 없습니다"));
                        employee.setLastLoginAt(LocalDateTime.now());
                        employeeRepository.save(employee);

                        log.info("로그인 성공: id={}, name={}", employee.getId(), employee.getName());

                        return ResponseEntity.ok(TokenResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshToken)
                                        .expiresIn(86400L)
                                        .employeeId(employee.getId())
                                        .employeeName(employee.getName())
                                        .role(employee.getRole().name())
                                        .build());

                } catch (BadCredentialsException e) {
                        log.warn("로그인 실패: {}", request.getEmployeeId());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new ErrorResponse("INVALID_CREDENTIALS", "사원 ID 또는 비밀번호가 올바르지 않습니다."));
                }
        }

        @Operation(summary = "회원가입", description = "새 사원 등록")
        @PostMapping("/register")
        public ResponseEntity<?> register(@Valid @RequestBody com.valumetric.dto.auth.RegisterRequest request) {
                log.info("회원가입 시도: {}", request.getEmail());

                // 이메일 중복 체크
                if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(new ErrorResponse("EMAIL_EXISTS", "이미 사용 중인 이메일입니다."));
                }

                // 새 사원 생성
                Employee employee = Employee.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .currentGrade(request.getCurrentGrade())
                                .currentSalary(request.getCurrentSalary())
                                .hireDate(java.time.LocalDate.now())
                                .isEnabled(true)
                                .role(Employee.Role.USER)
                                .build();

                employeeRepository.save(employee);
                log.info("회원가입 성공: id={}, email={}", employee.getId(), employee.getEmail());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new RegisterResponse(employee.getId(), employee.getName(), employee.getEmail(),
                                                "회원가입이 완료되었습니다."));
        }

        public record RegisterResponse(String id, String name, String email, String message) {
        }

        @Operation(summary = "토큰 갱신", description = "Refresh Token으로 Access Token 갱신")
        @PostMapping("/refresh")
        public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String bearerToken) {
                if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                        return ResponseEntity.badRequest()
                                        .body(new ErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰 형식입니다."));
                }

                String refreshToken = bearerToken.substring(7);

                if (!jwtTokenProvider.validateToken(refreshToken)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new ErrorResponse("EXPIRED_TOKEN", "토큰이 만료되었습니다."));
                }

                String employeeId = jwtTokenProvider.getUsernameFromToken(refreshToken);
                String newAccessToken = jwtTokenProvider.generateToken(employeeId, 86400000L);

                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("사원을 찾을 수 없습니다"));

                return ResponseEntity.ok(TokenResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                .expiresIn(86400L)
                                .employeeId(employee.getId())
                                .employeeName(employee.getName())
                                .role(employee.getRole().name())
                                .build());
        }

        @Operation(summary = "현재 사원 정보", description = "로그인한 사원 정보 조회")
        @GetMapping("/me")
        public ResponseEntity<?> getCurrentEmployee(Authentication authentication) {
                if (authentication == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new ErrorResponse("NOT_AUTHENTICATED", "인증되지 않았습니다."));
                }

                Employee employee = employeeRepository.findById(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("사원을 찾을 수 없습니다"));

                return ResponseEntity.ok(new EmployeeInfo(
                                employee.getId(),
                                employee.getName(),
                                employee.getEmail(),
                                employee.getCurrentGrade(),
                                employee.getRole().name()));
        }

        public record ErrorResponse(String code, String message) {
        }

        public record EmployeeInfo(String id, String name, String email, String grade, String role) {
        }
}
