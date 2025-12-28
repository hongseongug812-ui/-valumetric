package com.valumetric.service;

import com.valumetric.document.Employee;
import com.valumetric.document.SystemConfig;
import com.valumetric.dto.employee.EmployeeCreateRequest;
import com.valumetric.dto.employee.EmployeeResponse;
import com.valumetric.dto.employee.PerformanceLogRequest;
import com.valumetric.repository.EmployeeRepository;
import com.valumetric.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사원 관리 서비스 (MongoDB 버전)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SystemConfigRepository configRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 전체 사원 목록 조회
     */
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사원 상세 조회
     */
    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다: " + id));
    }

    /**
     * 신규 사원 등록
     */
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (request.getEmail() != null && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        Employee employee = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .currentGrade(request.getCurrentGrade())
                .currentSalary(request.getCurrentSalary())
                .hireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now())
                .role(parseRole(request.getRole()))
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .performanceLogs(new ArrayList<>())
                .scoreHistories(new ArrayList<>())
                .alerts(new ArrayList<>())
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("신규 사원 등록: id={}, name={}", saved.getId(), saved.getName());

        return toResponse(saved);
    }

    /**
     * 사원 정보 수정
     */
    public EmployeeResponse updateEmployee(String id, EmployeeCreateRequest request) {
        Employee employee = getEmployeeById(id);

        if (request.getName() != null)
            employee.setName(request.getName());
        if (request.getEmail() != null)
            employee.setEmail(request.getEmail());
        if (request.getCurrentGrade() != null)
            employee.setCurrentGrade(request.getCurrentGrade());
        if (request.getCurrentSalary() != null)
            employee.setCurrentSalary(request.getCurrentSalary());
        if (request.getHireDate() != null)
            employee.setHireDate(request.getHireDate());
        if (request.getRole() != null)
            employee.setRole(parseRole(request.getRole()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Employee saved = employeeRepository.save(employee);
        log.info("사원 정보 수정: id={}", id);

        return toResponse(saved);
    }

    /**
     * 사원 삭제 (비활성화)
     */
    public void deleteEmployee(String id) {
        Employee employee = getEmployeeById(id);
        employee.setIsEnabled(false);
        employeeRepository.save(employee);
        log.info("사원 비활성화: id={}", id);
    }

    /**
     * 월별 실적 입력
     */
    public Employee addPerformanceLog(PerformanceLogRequest request) {
        Employee employee = getEmployeeById(request.getEmployeeId());

        // 기존 동일 기간 데이터 제거
        employee.getPerformanceLogs().removeIf(
                log -> request.getPeriod().equals(log.getPeriod()));

        Employee.PerformanceLog perfLog = Employee.PerformanceLog.builder()
                .period(request.getPeriod())
                .targetSales(request.getTargetSales())
                .achievedSales(request.getAchievedSales())
                .profit(request.getProfit())
                .recordedAt(LocalDateTime.now())
                .build();

        employee.getPerformanceLogs().add(perfLog);

        Employee saved = employeeRepository.save(employee);
        log.info("실적 입력: employeeId={}, period={}", request.getEmployeeId(), request.getPeriod());

        return saved;
    }

    /**
     * 점수 기록 추가
     */
    public Employee addScoreHistory(String employeeId, String criteriaName,
            BigDecimal previousScore, BigDecimal scoreChange, String reason) {
        Employee employee = getEmployeeById(employeeId);

        Employee.ScoreHistory history = Employee.ScoreHistory.builder()
                .criteriaName(criteriaName)
                .previousScore(previousScore)
                .scoreChange(scoreChange)
                .newScore(previousScore.add(scoreChange))
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build();

        employee.getScoreHistories().add(history);
        return employeeRepository.save(employee);
    }

    /**
     * 초기 테스트 데이터 생성
     */
    public void createSampleData() {
        if (employeeRepository.count() > 0) {
            log.info("이미 사원 데이터가 존재합니다.");
            return;
        }

        // 관리자 계정
        createEmployee(EmployeeCreateRequest.builder()
                .name("관리자")
                .email("admin@valumetric.com")
                .password("admin123")
                .currentGrade("부장")
                .currentSalary(new BigDecimal("80000000"))
                .hireDate(LocalDate.of(2015, 3, 1))
                .role("ADMIN")
                .build());

        // 일반 사원들
        String[] names = { "김영희", "이철수", "박민지", "최동훈", "정서연" };
        String[] grades = { "대리", "과장", "사원", "차장", "대리" };
        BigDecimal[] salaries = {
                new BigDecimal("45000000"),
                new BigDecimal("55000000"),
                new BigDecimal("35000000"),
                new BigDecimal("65000000"),
                new BigDecimal("42000000")
        };

        for (int i = 0; i < names.length; i++) {
            Employee emp = createEmployeeEntity(names[i], grades[i], salaries[i], i + 1);
            addSamplePerformanceData(emp);
        }

        log.info("테스트 데이터 생성 완료: 총 {} 명", employeeRepository.count());
    }

    private Employee createEmployeeEntity(String name, String grade, BigDecimal salary, int seq) {
        Employee employee = Employee.builder()
                .name(name)
                .email(name.toLowerCase().replace(" ", "") + "@company.com")
                .password(passwordEncoder.encode("password123"))
                .currentGrade(grade)
                .currentSalary(salary)
                .hireDate(LocalDate.now().minusYears(seq))
                .role(Employee.Role.USER)
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .performanceLogs(new ArrayList<>())
                .scoreHistories(new ArrayList<>())
                .alerts(new ArrayList<>())
                .build();

        return employeeRepository.save(employee);
    }

    private void addSamplePerformanceData(Employee employee) {
        String[] periods = { "2024-07", "2024-08", "2024-09", "2024-10", "2024-11", "2024-12" };

        for (String period : periods) {
            BigDecimal target = employee.getCurrentSalary()
                    .divide(BigDecimal.valueOf(12), 0, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("2")); // 월급의 2배를 목표로

            // 랜덤 달성률 (70% ~ 130%)
            double rate = 0.7 + Math.random() * 0.6;
            BigDecimal achieved = target.multiply(BigDecimal.valueOf(rate))
                    .setScale(0, java.math.RoundingMode.HALF_UP);
            BigDecimal profit = achieved.multiply(new BigDecimal("0.15"));

            Employee.PerformanceLog log = Employee.PerformanceLog.builder()
                    .period(period)
                    .targetSales(target)
                    .achievedSales(achieved)
                    .profit(profit)
                    .recordedAt(LocalDateTime.now())
                    .build();

            employee.getPerformanceLogs().add(log);
        }

        employeeRepository.save(employee);
    }

    private EmployeeResponse toResponse(Employee emp) {
        return EmployeeResponse.builder()
                .id(emp.getId())
                .name(emp.getName())
                .email(emp.getEmail())
                .currentGrade(emp.getCurrentGrade())
                .currentSalary(emp.getCurrentSalary())
                .hireDate(emp.getHireDate())
                .role(emp.getRole().name())
                .isEnabled(emp.getIsEnabled())
                .performanceLogCount(emp.getPerformanceLogs().size())
                .alertCount((int) emp.getAlerts().stream().filter(a -> !a.getIsResolved()).count())
                .build();
    }

    private Employee.Role parseRole(String role) {
        if (role == null || role.isBlank())
            return Employee.Role.USER;
        try {
            return Employee.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Employee.Role.USER;
        }
    }

    /**
     * 전체 사원 점수 초기화 (테스트용)
     */
    public void initializeScores() {
        List<Employee> employees = employeeRepository.findAll();

        for (Employee emp : employees) {
            // 초기 점수 랜덤 (600 ~ 950)
            int baseScore = 600 + (int) (Math.random() * 350);

            Employee.ScoreHistory initial = Employee.ScoreHistory.builder()
                    .criteriaName("종합평가")
                    .previousScore(BigDecimal.ZERO)
                    .scoreChange(new BigDecimal(baseScore))
                    .newScore(new BigDecimal(baseScore))
                    .reason("초기 점수 설정")
                    .changedAt(LocalDateTime.now())
                    .build();

            emp.getScoreHistories().add(initial);
            employeeRepository.save(emp);
        }

        log.info("점수 초기화 완료: {} 명", employees.size());
    }
}
