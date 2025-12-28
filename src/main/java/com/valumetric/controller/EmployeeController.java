package com.valumetric.controller;

import com.valumetric.document.Employee;
import com.valumetric.dto.employee.EmployeeCreateRequest;
import com.valumetric.dto.employee.EmployeeResponse;
import com.valumetric.dto.employee.PerformanceLogRequest;
import com.valumetric.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사원 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee", description = "사원 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "전체 사원 목록 조회")
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        log.info("사원 목록 조회");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(summary = "사원 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String id) {
        log.info("사원 조회: id={}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "신규 사원 등록")
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        log.info("사원 등록: name={}", request.getName());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "사원 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable String id,
            @RequestBody EmployeeCreateRequest request) {
        log.info("사원 수정: id={}", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @Operation(summary = "사원 삭제 (비활성화)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        log.info("사원 삭제: id={}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "월별 실적 입력")
    @PostMapping("/performance")
    public ResponseEntity<Employee> addPerformanceLog(
            @Valid @RequestBody PerformanceLogRequest request) {
        log.info("실적 입력: employeeId={}, period={}", request.getEmployeeId(), request.getPeriod());
        return ResponseEntity.ok(employeeService.addPerformanceLog(request));
    }

    @Operation(summary = "테스트 데이터 생성")
    @PostMapping("/sample-data")
    public ResponseEntity<String> createSampleData() {
        log.info("테스트 데이터 생성 요청");
        employeeService.createSampleData();
        return ResponseEntity.ok("테스트 데이터 생성 완료");
    }

    @Operation(summary = "전체 사원 점수 초기화")
    @PostMapping("/init-scores")
    public ResponseEntity<String> initScores() {
        log.info("전체 사원 점수 초기화 요청");
        employeeService.initializeScores();
        return ResponseEntity.ok("점수 초기화 완료");
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
