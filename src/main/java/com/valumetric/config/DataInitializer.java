package com.valumetric.config;

import com.valumetric.document.Employee;
import com.valumetric.document.SystemConfig;
import com.valumetric.repository.EmployeeRepository;
import com.valumetric.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 서버 시작 시 샘플 데이터 자동 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final SystemConfigRepository configRepository;

    private final Random random = new Random();

    private static final String[] KOREAN_LAST_NAMES = { "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임" };
    private static final String[] KOREAN_FIRST_NAMES = { "민준", "서연", "예준", "지우", "도윤", "서현", "시우", "하은", "주원", "지민" };
    private static final String[] GRADES = { "사원", "대리", "과장", "차장", "부장" };
    private static final String[] DEPARTMENTS = { "영업", "마케팅", "개발", "기획", "인사", "재무" };

    @Override
    public void run(ApplicationArguments args) {
        initializeSystemConfig();
        initializeEmployees();
    }

    /**
     * 시스템 설정 초기화 (AHP 가중치 포함)
     */
    private void initializeSystemConfig() {
        SystemConfig existing = configRepository.getDefaultConfig();
        if (existing.getId() != null && existing.getEvaluationCriteria() != null
                && !existing.getEvaluationCriteria().isEmpty()) {
            log.info("시스템 설정이 이미 존재합니다. 스킵.");
            return;
        }

        SystemConfig config = SystemConfig.createDefault();
        configRepository.save(config);
        log.info("✅ 시스템 기본 설정 생성 완료 (AHP 가중치 포함)");
    }

    /**
     * 50명의 사원 데이터 생성 (3~5명은 위험군)
     */
    private void initializeEmployees() {
        long count = employeeRepository.count();
        if (count >= 50) {
            log.info("사원 데이터가 이미 {}명 존재합니다. 생일 데이터 확인 중...", count);

            // 기존 사원들에게 birthDate가 없으면 추가
            updateMissingBirthDates();
            return;
        }

        // 기존 데이터 삭제 후 새로 생성
        employeeRepository.deleteAll();
        log.info("기존 사원 데이터 삭제 완료");

        List<Employee> employees = new ArrayList<>();

        // 정상 사원 45명
        for (int i = 1; i <= 45; i++) {
            employees.add(createNormalEmployee(i));
        }

        // 위험군 사원 5명 (적자 사원)
        for (int i = 46; i <= 50; i++) {
            employees.add(createRedZoneEmployee(i));
        }

        employeeRepository.saveAll(employees);
        log.info("✅ 사원 {} 명 생성 완료 (위험군 5명 포함)", employees.size());
    }

    /**
     * 정상 사원 생성 (HCROI > 1.0, 좋은 실적)
     */
    private Employee createNormalEmployee(int index) {
        String name = KOREAN_LAST_NAMES[random.nextInt(KOREAN_LAST_NAMES.length)] +
                KOREAN_FIRST_NAMES[random.nextInt(KOREAN_FIRST_NAMES.length)];
        String grade = GRADES[random.nextInt(GRADES.length)];

        // 연봉 4000만 ~ 9000만
        BigDecimal salary = new BigDecimal(40000000 + random.nextInt(50000000));

        Employee emp = Employee.builder()
                .name(name)
                .email(String.format("employee%d@valumetric.com", index))
                .currentGrade(grade)
                .currentSalary(salary)
                .hireDate(LocalDate.now().minusYears(random.nextInt(10) + 1))
                .birthDate(generateRandomBirthDate()) // 생일 추가
                .isEnabled(true)
                .performanceLogs(new ArrayList<>())
                .scoreHistories(new ArrayList<>())
                .alerts(new ArrayList<>())
                .build();

        // 최근 6개월 실적 로그 생성 (좋은 실적)
        generatePerformanceLogs(emp, true);

        // 점수 이력 생성 (700~950)
        generateScoreHistory(emp, 700 + random.nextInt(250));

        return emp;
    }

    /**
     * 위험군 사원 생성 (HCROI < 1.0, 저조한 실적)
     */
    private Employee createRedZoneEmployee(int index) {
        String name = KOREAN_LAST_NAMES[random.nextInt(KOREAN_LAST_NAMES.length)] +
                KOREAN_FIRST_NAMES[random.nextInt(KOREAN_FIRST_NAMES.length)];

        // 연봉이 높지만 실적이 저조한 경우
        BigDecimal salary = new BigDecimal(70000000 + random.nextInt(30000000));

        Employee emp = Employee.builder()
                .name(name)
                .email(String.format("employee%d@valumetric.com", index))
                .currentGrade(GRADES[random.nextInt(3) + 2]) // 과장 이상
                .currentSalary(salary)
                .hireDate(LocalDate.now().minusYears(random.nextInt(5) + 5))
                .birthDate(generateRandomBirthDate()) // 생일 추가
                .isEnabled(true)
                .performanceLogs(new ArrayList<>())
                .scoreHistories(new ArrayList<>())
                .alerts(new ArrayList<>())
                .build();

        // 최근 6개월 실적 로그 생성 (저조한 실적)
        generatePerformanceLogs(emp, false);

        // 점수 이력 생성 (500~700 - 위험군)
        generateScoreHistory(emp, 500 + random.nextInt(200));

        // 경고 알림 생성
        generateAlert(emp);

        return emp;
    }

    /**
     * 6개월치 실적 로그 생성
     */
    private void generatePerformanceLogs(Employee emp, boolean isGoodPerformer) {
        YearMonth currentMonth = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            String period = targetMonth.toString();

            BigDecimal monthlySalary = emp.getCurrentSalary()
                    .divide(BigDecimal.valueOf(12), 0, java.math.RoundingMode.HALF_UP);

            // 목표 매출 = 월급 * 2 ~ 3
            BigDecimal targetSales = monthlySalary.multiply(
                    BigDecimal.valueOf(2 + random.nextDouble()));

            BigDecimal achievedSales;
            if (isGoodPerformer) {
                // 정상: 목표의 100% ~ 150% 달성
                achievedSales = targetSales.multiply(
                        BigDecimal.valueOf(1.0 + random.nextDouble() * 0.5));
            } else {
                // 위험군: 목표의 40% ~ 70% 달성 (적자)
                achievedSales = targetSales.multiply(
                        BigDecimal.valueOf(0.4 + random.nextDouble() * 0.3));
            }

            Employee.PerformanceLog log = Employee.PerformanceLog.builder()
                    .period(period)
                    .targetSales(targetSales.setScale(0, java.math.RoundingMode.HALF_UP))
                    .achievedSales(achievedSales.setScale(0, java.math.RoundingMode.HALF_UP))
                    .recordedAt(LocalDateTime.now().minusMonths(i))
                    .build();

            emp.getPerformanceLogs().add(log);
        }
    }

    /**
     * 점수 이력 생성
     */
    private void generateScoreHistory(Employee emp, int baseScore) {
        Employee.ScoreHistory history = Employee.ScoreHistory.builder()
                .criteriaName("종합평가")
                .previousScore(BigDecimal.ZERO)
                .scoreChange(new BigDecimal(baseScore))
                .newScore(new BigDecimal(baseScore))
                .reason("초기 점수 설정")
                .changedAt(LocalDateTime.now())
                .build();

        emp.getScoreHistories().add(history);
    }

    /**
     * 경고 알림 생성 (위험군용)
     */
    private void generateAlert(Employee emp) {
        Employee.Alert alert = Employee.Alert.builder()
                .alertType(Employee.AlertType.LOW_HCROI)
                .message("HCROI 기준 미달 - 인건비 대비 매출 부족")
                .isResolved(false)
                .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                .build();

        emp.getAlerts().add(alert);
    }

    /**
     * 랜덤 생일 생성 (25~55세)
     * 1월 생일자를 더 많이 생성하여 곧 다가오는 생일 테스트 용이
     */
    private LocalDate generateRandomBirthDate() {
        int year = LocalDate.now().getYear() - (25 + random.nextInt(30)); // 25~55세
        int month;

        // 30% 확률로 현재 달 또는 다음 달 생일로 설정
        if (random.nextInt(100) < 30) {
            month = LocalDate.now().getMonthValue();
            if (random.nextBoolean()) {
                month = (month % 12) + 1; // 다음 달
            }
        } else {
            month = 1 + random.nextInt(12);
        }

        int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
        int day = 1 + random.nextInt(maxDay);

        return LocalDate.of(year, month, day);
    }

    /**
     * 기존 사원 중 birthDate가 없는 사원에게 생일 추가
     */
    private void updateMissingBirthDates() {
        List<Employee> employees = employeeRepository.findAll();
        int updatedCount = 0;

        for (Employee emp : employees) {
            if (emp.getBirthDate() == null) {
                emp.setBirthDate(generateRandomBirthDate());
                employeeRepository.save(emp);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            log.info("✅ {}명의 사원에게 생일 데이터 추가 완료", updatedCount);
        } else {
            log.info("모든 사원에게 이미 생일 데이터가 있습니다.");
        }
    }
}
