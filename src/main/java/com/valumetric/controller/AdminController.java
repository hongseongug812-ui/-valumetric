package com.valumetric.controller;

import com.valumetric.document.SystemConfig;
import com.valumetric.dto.admin.AhpMatrixUpdateRequest;
import com.valumetric.dto.admin.AhpWeightResponse;
import com.valumetric.dto.admin.SalaryConfigUpdateRequest;
import com.valumetric.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 REST API 컨트롤러 (MongoDB 버전)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Admin", description = "관리자 API - AHP/급여 설정 (🔐 ADMIN 권한 필요)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

        private final AdminService adminService;

        @Operation(summary = "급여 설정 조회")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = SystemConfig.class)))
        })
        @GetMapping("/salary-config")
        public ResponseEntity<SystemConfig> getSalaryConfig() {
                log.info("급여 설정 조회");
                SystemConfig config = adminService.getSalaryConfig();
                return ResponseEntity.ok(config);
        }

        @Operation(summary = "급여 설정 수정")
        @PutMapping("/salary-config")
        public ResponseEntity<SystemConfig> updateSalaryConfig(
                        @Valid @RequestBody SalaryConfigUpdateRequest request) {
                log.info("급여 설정 수정 요청");
                SystemConfig config = adminService.updateSalaryConfig(request);
                return ResponseEntity.ok(config);
        }

        @Operation(summary = "현재 AHP 가중치 조회")
        @GetMapping("/ahp/weights")
        public ResponseEntity<AhpWeightResponse> getAhpWeights() {
                log.info("AHP 가중치 조회");
                AhpWeightResponse response = adminService.getCurrentAhpWeights();
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "AHP 쌍대비교로 가중치 계산")
        @PostMapping("/ahp/calculate")
        public ResponseEntity<AhpWeightResponse> calculateAhpWeights(
                        @Valid @RequestBody AhpMatrixUpdateRequest request) {
                log.info("AHP 쌍대비교 계산 요청: matrixSize={}", request.getMatrixSize());
                AhpWeightResponse response = adminService.calculateAndSaveAhpWeights(request);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "AHP 가중치 직접 설정")
        @PutMapping("/ahp/weights")
        public ResponseEntity<AhpWeightResponse> setAhpWeights(
                        @RequestBody DirectWeightRequest request) {
                log.info("AHP 가중치 직접 설정 요청");
                AhpWeightResponse response = adminService.saveDirectWeights(
                                request.criteriaNames(),
                                request.weights());
                return ResponseEntity.ok(response);
        }

        public record DirectWeightRequest(String[] criteriaNames, double[] weights) {
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
                log.error("잘못된 요청: {}", e.getMessage());
                return ResponseEntity.badRequest()
                                .body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
        }

        public record ErrorResponse(String code, String message) {
        }
}
