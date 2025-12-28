package com.valumetric.controller;

import com.valumetric.dto.dashboard.*;
import com.valumetric.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대시보드 REST API 컨트롤러 (MongoDB 버전)
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "대시보드 API - 사원 성과 현황 조회")
public class DashboardController {

        private final DashboardService dashboardService;

        @Operation(summary = "대시보드 전체 데이터 조회")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = DashboardResponseDto.class)))
        })
        @GetMapping
        public ResponseEntity<DashboardResponseDto> getDashboard() {
                log.info("대시보드 전체 데이터 조회");
                DashboardResponseDto response = dashboardService.getDashboardData();
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "대시보드 요약 정보 조회")
        @GetMapping("/summary")
        public ResponseEntity<DashboardSummaryDto> getSummary() {
                log.info("대시보드 요약 정보 조회");
                DashboardSummaryDto summary = dashboardService.getSummary();
                return ResponseEntity.ok(summary);
        }

        @Operation(summary = "위험군(Red Zone) 사원 리스트 조회")
        @GetMapping("/red-zone")
        public ResponseEntity<List<RedZoneEmployeeDto>> getRedZoneEmployees() {
                log.info("위험군 사원 리스트 조회");
                List<RedZoneEmployeeDto> redZoneList = dashboardService.getRedZoneEmployees();
                return ResponseEntity.ok(redZoneList);
        }

        @Operation(summary = "사원별 6개월 추이 데이터 조회")
        @GetMapping("/trend/{employeeId}")
        public ResponseEntity<EmployeeTrendDto> getEmployeeTrend(
                        @Parameter(description = "사원 ID (MongoDB ObjectId)", required = true) @PathVariable String employeeId) {
                log.info("사원 추이 데이터 조회: employeeId={}", employeeId);
                EmployeeTrendDto trend = dashboardService.getEmployeeTrend(employeeId);
                return ResponseEntity.ok(trend);
        }

        @Operation(summary = "월별 매출/인건비 추이 (최근 6개월)")
        @GetMapping("/monthly-trend")
        public ResponseEntity<MonthlyTrendResponse> getMonthlyTrend() {
                log.info("월별 추이 데이터 조회");
                MonthlyTrendResponse trend = dashboardService.getMonthlyTrend();
                return ResponseEntity.ok(trend);
        }

        @Operation(summary = "손익분기점(BEP) 달성 현황")
        @GetMapping("/bep-status")
        public ResponseEntity<BepStatusDto> getBepStatus() {
                log.info("BEP 달성 현황 조회");
                BepStatusDto bepStatus = dashboardService.getBepStatus();
                return ResponseEntity.ok(bepStatus);
        }

        @Operation(summary = "AHP 평가 가중치 조회")
        @GetMapping("/ahp-weights")
        public ResponseEntity<AhpWeightsDto> getAhpWeights() {
                log.info("AHP 가중치 조회");
                AhpWeightsDto weights = dashboardService.getAhpWeights();
                return ResponseEntity.ok(weights);
        }

        @Operation(summary = "곧 다가오는 생일 조회", description = "앞으로 30일 내 생일인 사원 목록")
        @GetMapping("/birthdays")
        public ResponseEntity<List<UpcomingBirthdayDto>> getUpcomingBirthdays() {
                log.info("곧 다가오는 생일 조회");
                List<UpcomingBirthdayDto> birthdays = dashboardService.getUpcomingBirthdays();
                return ResponseEntity.ok(birthdays);
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
