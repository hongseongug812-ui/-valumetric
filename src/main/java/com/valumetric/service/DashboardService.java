package com.valumetric.service;

import com.valumetric.calculator.HcroiCalculator;
import com.valumetric.document.Employee;
import com.valumetric.document.SystemConfig;
import com.valumetric.dto.dashboard.*;
import com.valumetric.repository.EmployeeRepository;
import com.valumetric.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 대시보드 서비스 (MongoDB 버전)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final SystemConfigRepository configRepository;
    private final HcroiCalculator hcroiCalculator;

    private static final BigDecimal HCROI_THRESHOLD = BigDecimal.ONE;
    private static final BigDecimal SCORE_THRESHOLD = new BigDecimal("700");
    private static final BigDecimal HCROI_EXCELLENT = new BigDecimal("1.5");
    private static final BigDecimal SCORE_EXCELLENT = new BigDecimal("900");

    /**
     * 대시보드 전체 데이터 조회
     */
    public DashboardResponseDto getDashboardData() {
        DashboardSummaryDto summary = getSummary();
        List<RedZoneEmployeeDto> redZoneEmployees = getRedZoneEmployees();
        List<TopPerformerDto> topPerformers = getTopPerformers();
        List<WatchListEmployeeDto> watchList = getWatchList();

        return DashboardResponseDto.builder()
                .summary(summary)
                .redZoneEmployees(redZoneEmployees)
                .topPerformers(topPerformers)
                .watchList(watchList)
                .build();
    }

    /**
     * 대시보드 요약 정보 조회
     */
    public DashboardSummaryDto getSummary() {
        List<Employee> employees = employeeRepository.findByIsEnabledTrue();
        String currentPeriod = YearMonth.now().toString();

        long totalCount = employees.size();
        SystemConfig config = configRepository.getDefaultConfig();

        List<BigDecimal> hcroiList = new ArrayList<>();
        List<BigDecimal> scoreList = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long unresolvedAlertCount = 0;

        for (Employee emp : employees) {
            // 현재 월 실적 조회 (내장 리스트에서)
            Optional<Employee.PerformanceLog> logOpt = emp.getPerformanceLogs().stream()
                    .filter(l -> currentPeriod.equals(l.getPeriod()))
                    .findFirst();

            if (logOpt.isPresent()) {
                Employee.PerformanceLog perfLog = logOpt.get();
                totalRevenue = totalRevenue.add(perfLog.getAchievedSales());

                BigDecimal monthlySalary = emp.getCurrentSalary()
                        .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
                BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());

                try {
                    HcroiCalculator.HcroiResult result = hcroiCalculator.calculateWithBenefits(
                            perfLog.getAchievedSales(),
                            monthlySalary,
                            benefitCost,
                            config.getFixedCostPerPerson(),
                            config.getTargetProfitRate());
                    hcroiList.add(result.getHcroiIndex());
                } catch (Exception e) {
                    log.warn("HCROI 계산 실패: employeeId={}", emp.getId());
                }
            }

            // 최근 점수 조회
            if (!emp.getScoreHistories().isEmpty()) {
                scoreList.add(emp.getScoreHistories().get(emp.getScoreHistories().size() - 1).getNewScore());
            }

            // 미해결 경고 수
            unresolvedAlertCount += emp.getAlerts().stream()
                    .filter(a -> !a.getIsResolved())
                    .count();
        }

        BigDecimal avgHcroi = calculateAverage(hcroiList);
        BigDecimal avgScore = calculateAverage(scoreList);
        long redZoneCount = countRedZone(hcroiList, scoreList);

        return DashboardSummaryDto.builder()
                .totalEmployeeCount(totalCount)
                .averageHcroi(avgHcroi)
                .averageScore(avgScore)
                .redZoneCount(redZoneCount)
                .unresolvedAlertCount(unresolvedAlertCount)
                .companyTotalRevenue(totalRevenue)
                .build();
    }

    /**
     * 위험군 사원 리스트 조회
     */
    public List<RedZoneEmployeeDto> getRedZoneEmployees() {
        List<Employee> employees = employeeRepository.findByIsEnabledTrue();
        String currentPeriod = YearMonth.now().toString();
        List<RedZoneEmployeeDto> redZoneList = new ArrayList<>();

        SystemConfig config = configRepository.getDefaultConfig();

        for (Employee emp : employees) {
            Optional<Employee.PerformanceLog> logOpt = emp.getPerformanceLogs().stream()
                    .filter(l -> currentPeriod.equals(l.getPeriod()))
                    .findFirst();

            BigDecimal currentHcroi = null;
            BigDecimal achievementRate = null;

            if (logOpt.isPresent()) {
                Employee.PerformanceLog perfLog = logOpt.get();
                BigDecimal monthlySalary = emp.getCurrentSalary()
                        .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
                BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());

                try {
                    HcroiCalculator.HcroiResult result = hcroiCalculator.calculateWithBenefits(
                            perfLog.getAchievedSales(),
                            monthlySalary,
                            benefitCost,
                            config.getFixedCostPerPerson(),
                            config.getTargetProfitRate());
                    currentHcroi = result.getHcroiIndex();
                    achievementRate = result.getTargetAchievementRate();
                } catch (Exception e) {
                    continue;
                }
            }

            BigDecimal currentScore = emp.getScoreHistories().isEmpty()
                    ? BigDecimal.ZERO
                    : emp.getScoreHistories().get(emp.getScoreHistories().size() - 1).getNewScore();

            boolean lowHcroi = currentHcroi != null && currentHcroi.compareTo(HCROI_THRESHOLD) < 0;
            boolean lowScore = currentScore.compareTo(SCORE_THRESHOLD) < 0;

            if (lowHcroi || lowScore) {
                String riskLevel = (lowHcroi && lowScore) ? "CRITICAL" : "WARNING";
                String riskReason = (lowHcroi && lowScore)
                        ? "HCROI 및 점수 모두 기준 미달"
                        : lowHcroi ? "HCROI 기준 미달 (< 1.0)" : "점수 기준 미달 (< 700점)";

                long unresolvedCount = emp.getAlerts().stream()
                        .filter(a -> !a.getIsResolved())
                        .count();

                redZoneList.add(RedZoneEmployeeDto.builder()
                        .employeeId(emp.getId())
                        .employeeName(emp.getName())
                        .currentGrade(emp.getCurrentGrade())
                        .currentSalary(emp.getCurrentSalary())
                        .currentHcroi(currentHcroi)
                        .currentScore(currentScore)
                        .targetAchievementRate(achievementRate)
                        .riskLevel(riskLevel)
                        .riskReason(riskReason)
                        .unresolvedAlertCount(unresolvedCount)
                        .build());
            }
        }

        redZoneList.sort((a, b) -> {
            int levelCompare = b.getRiskLevel().compareTo(a.getRiskLevel());
            return levelCompare != 0 ? levelCompare : a.getCurrentScore().compareTo(b.getCurrentScore());
        });

        return redZoneList;
    }

    /**
     * 우수 사원 리스트 조회
     */
    public List<TopPerformerDto> getTopPerformers() {
        List<Employee> employees = employeeRepository.findByIsEnabledTrue();
        String currentPeriod = YearMonth.now().toString();
        List<TopPerformerDto> topList = new ArrayList<>();

        SystemConfig config = configRepository.getDefaultConfig();

        for (Employee emp : employees) {
            Optional<Employee.PerformanceLog> logOpt = emp.getPerformanceLogs().stream()
                    .filter(l -> currentPeriod.equals(l.getPeriod()))
                    .findFirst();

            BigDecimal currentHcroi = null;
            BigDecimal achievementRate = null;

            if (logOpt.isPresent()) {
                Employee.PerformanceLog perfLog = logOpt.get();
                BigDecimal monthlySalary = emp.getCurrentSalary()
                        .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
                BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());

                try {
                    HcroiCalculator.HcroiResult result = hcroiCalculator.calculateWithBenefits(
                            perfLog.getAchievedSales(),
                            monthlySalary,
                            benefitCost,
                            config.getFixedCostPerPerson(),
                            config.getTargetProfitRate());
                    currentHcroi = result.getHcroiIndex();
                    achievementRate = result.getTargetAchievementRate();
                } catch (Exception e) {
                    continue;
                }
            }

            BigDecimal currentScore = emp.getScoreHistories().isEmpty()
                    ? BigDecimal.ZERO
                    : emp.getScoreHistories().get(emp.getScoreHistories().size() - 1).getNewScore();

            boolean highHcroi = currentHcroi != null && currentHcroi.compareTo(HCROI_EXCELLENT) >= 0;
            boolean highScore = currentScore.compareTo(SCORE_EXCELLENT) >= 0;

            if (highHcroi || highScore) {
                String level = (highHcroi && highScore) ? "OUTSTANDING" : "EXCELLENT";
                String achievement = (highHcroi && highScore)
                        ? "HCROI 및 점수 모두 최우수"
                        : highHcroi ? "HCROI 우수 (≥ 1.5)" : "점수 우수 (≥ 900점)";

                // 연속 달성 개월 수 계산
                int consecutive = 0;
                for (int i = emp.getPerformanceLogs().size() - 1; i >= 0 && consecutive < 6; i--) {
                    Employee.PerformanceLog log = emp.getPerformanceLogs().get(i);
                    if (log.getAchievedSales() != null && log.getTargetSales() != null &&
                            log.getAchievedSales().compareTo(log.getTargetSales()) >= 0) {
                        consecutive++;
                    } else {
                        break;
                    }
                }

                topList.add(TopPerformerDto.builder()
                        .employeeId(emp.getId())
                        .employeeName(emp.getName())
                        .currentGrade(emp.getCurrentGrade())
                        .currentSalary(emp.getCurrentSalary())
                        .currentHcroi(currentHcroi)
                        .currentScore(currentScore)
                        .targetAchievementRate(achievementRate)
                        .performanceLevel(level)
                        .achievement(achievement)
                        .consecutiveMonths(consecutive)
                        .build());
            }
        }

        topList.sort((a, b) -> {
            int levelCompare = b.getPerformanceLevel().compareTo(a.getPerformanceLevel());
            return levelCompare != 0 ? levelCompare : b.getCurrentHcroi().compareTo(a.getCurrentHcroi());
        });

        return topList;
    }

    /**
     * 사원 추이 데이터 조회
     */
    public EmployeeTrendDto getEmployeeTrend(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다: " + employeeId));

        SystemConfig config = configRepository.getDefaultConfig();
        List<EmployeeTrendDto.MonthlyTrendData> trendDataList = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            String period = targetMonth.toString();

            Optional<Employee.PerformanceLog> logOpt = employee.getPerformanceLogs().stream()
                    .filter(l -> period.equals(l.getPeriod()))
                    .findFirst();

            BigDecimal revenue = BigDecimal.ZERO;
            BigDecimal hcroi = BigDecimal.ZERO;
            BigDecimal achievementRate = BigDecimal.ZERO;

            if (logOpt.isPresent()) {
                revenue = logOpt.get().getAchievedSales();
                BigDecimal monthlySalary = employee.getCurrentSalary()
                        .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
                BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());

                try {
                    HcroiCalculator.HcroiResult result = hcroiCalculator.calculateWithBenefits(
                            revenue, monthlySalary, benefitCost,
                            config.getFixedCostPerPerson(), config.getTargetProfitRate());
                    hcroi = result.getHcroiIndex();
                    achievementRate = result.getTargetAchievementRate();
                } catch (Exception e) {
                    log.warn("추이 HCROI 계산 실패: month={}", targetMonth);
                }
            }

            BigDecimal score = BigDecimal.ZERO;
            // 해당 월의 점수 합계
            for (Employee.ScoreHistory sh : employee.getScoreHistories()) {
                if (sh.getChangedAt() != null &&
                        YearMonth.from(sh.getChangedAt()).equals(targetMonth)) {
                    score = score.add(sh.getScoreChange());
                }
            }

            trendDataList.add(EmployeeTrendDto.MonthlyTrendData.builder()
                    .period(period)
                    .revenue(revenue)
                    .score(score)
                    .hcroi(hcroi)
                    .achievementRate(achievementRate)
                    .build());
        }

        return EmployeeTrendDto.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .currentGrade(employee.getCurrentGrade())
                .trendData(trendDataList)
                .build();
    }

    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values.isEmpty())
            return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private long countRedZone(List<BigDecimal> hcroiList, List<BigDecimal> scoreList) {
        long count = hcroiList.stream().filter(h -> h.compareTo(HCROI_THRESHOLD) < 0).count();
        count += scoreList.stream().filter(s -> s.compareTo(SCORE_THRESHOLD) < 0).count();
        return count;
    }

    /**
     * 월별 매출/인건비 추이 조회 (최근 6개월)
     */
    public MonthlyTrendResponse getMonthlyTrend() {
        List<Employee> employees = employeeRepository.findByIsEnabledTrue();
        SystemConfig config = configRepository.getDefaultConfig();
        YearMonth currentMonth = YearMonth.now();

        List<MonthlyTrendResponse.MonthlyData> dataList = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            String period = targetMonth.toString();

            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalLaborCost = BigDecimal.ZERO;
            List<BigDecimal> hcroiList = new ArrayList<>();
            int empCount = 0;

            for (Employee emp : employees) {
                // 해당 월 실적 조회
                Optional<Employee.PerformanceLog> logOpt = emp.getPerformanceLogs().stream()
                        .filter(l -> period.equals(l.getPeriod()))
                        .findFirst();

                if (logOpt.isPresent()) {
                    Employee.PerformanceLog log = logOpt.get();
                    totalRevenue = totalRevenue.add(log.getAchievedSales());
                    empCount++;

                    // 월 인건비 계산
                    BigDecimal monthlySalary = emp.getCurrentSalary()
                            .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
                    BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());
                    totalLaborCost = totalLaborCost.add(monthlySalary).add(benefitCost);

                    // HCROI 계산
                    try {
                        HcroiCalculator.HcroiResult result = hcroiCalculator.calculateWithBenefits(
                                log.getAchievedSales(),
                                monthlySalary,
                                benefitCost,
                                config.getFixedCostPerPerson(),
                                config.getTargetProfitRate());
                        hcroiList.add(result.getHcroiIndex());
                    } catch (Exception e) {
                        // skip
                    }
                }
            }

            BigDecimal avgHcroi = calculateAverage(hcroiList);

            dataList.add(MonthlyTrendResponse.MonthlyData.builder()
                    .period(period)
                    .totalRevenue(totalRevenue)
                    .totalLaborCost(totalLaborCost)
                    .averageHcroi(avgHcroi)
                    .employeeCount(empCount)
                    .build());
        }

        return MonthlyTrendResponse.builder()
                .data(dataList)
                .build();
    }

    /**
     * 손익분기점(BEP) 달성 현황 조회
     */
    public BepStatusDto getBepStatus() {
        List<Employee> employees = employeeRepository.findByIsEnabledTrue();
        SystemConfig config = configRepository.getDefaultConfig();
        String currentPeriod = YearMonth.now().toString();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalTarget = BigDecimal.ZERO;
        BigDecimal totalBep = BigDecimal.ZERO;
        int empCount = 0;

        for (Employee emp : employees) {
            Optional<Employee.PerformanceLog> logOpt = emp.getPerformanceLogs().stream()
                    .filter(l -> currentPeriod.equals(l.getPeriod()))
                    .findFirst();

            if (logOpt.isPresent()) {
                Employee.PerformanceLog log = logOpt.get();
                totalRevenue = totalRevenue.add(log.getAchievedSales());
                totalTarget = totalTarget.add(log.getTargetSales());
                empCount++;

                // BEP 계산: 월 인건비 / 목표이익률
                BigDecimal monthlySalary = emp.getCurrentSalary()
                        .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
                BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());
                BigDecimal totalCost = monthlySalary.add(benefitCost).add(config.getFixedCostPerPerson());

                BigDecimal bep = totalCost.divide(config.getTargetProfitRate(), 0, RoundingMode.HALF_UP);
                totalBep = totalBep.add(bep);
            }
        }

        // 달성률 계산
        BigDecimal achievementRate = BigDecimal.ZERO;
        BigDecimal bepAchievementRate = BigDecimal.ZERO;

        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            achievementRate = totalRevenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalTarget, 2, RoundingMode.HALF_UP);
        }
        if (totalBep.compareTo(BigDecimal.ZERO) > 0) {
            bepAchievementRate = totalRevenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalBep, 2, RoundingMode.HALF_UP);
        }

        BigDecimal remainingToBep = totalBep.subtract(totalRevenue);
        BigDecimal remainingToTarget = totalTarget.subtract(totalRevenue);

        return BepStatusDto.builder()
                .period(currentPeriod)
                .targetRevenue(totalTarget)
                .currentRevenue(totalRevenue)
                .bepRevenue(totalBep)
                .achievementRate(achievementRate)
                .bepAchievementRate(bepAchievementRate)
                .remainingToBep(remainingToBep)
                .remainingToTarget(remainingToTarget)
                .bepAchieved(totalRevenue.compareTo(totalBep) >= 0)
                .targetAchieved(totalRevenue.compareTo(totalTarget) >= 0)
                .contributingEmployees(empCount)
                .build();
    }

    /**
     * AHP 가중치 정보 조회
     */
    public AhpWeightsDto getAhpWeights() {
        SystemConfig config = configRepository.getDefaultConfig();
        List<SystemConfig.EvaluationCriteria> criteriaList = config.getEvaluationCriteria();

        List<AhpWeightsDto.CriteriaWeight> weights = new ArrayList<>();

        for (SystemConfig.EvaluationCriteria c : criteriaList) {
            if (c.getIsActive() == null || c.getIsActive()) {
                weights.add(AhpWeightsDto.CriteriaWeight.builder()
                        .name(c.getName())
                        .description(c.getDescription())
                        .weight(c.getWeight())
                        .percentage((int) Math.round(c.getWeight() * 100))
                        .displayOrder(c.getDisplayOrder())
                        .build());
            }
        }

        // displayOrder로 정렬
        weights.sort((a, b) -> {
            int o1 = a.getDisplayOrder() != null ? a.getDisplayOrder() : 999;
            int o2 = b.getDisplayOrder() != null ? b.getDisplayOrder() : 999;
            return Integer.compare(o1, o2);
        });

        return AhpWeightsDto.builder()
                .criteria(weights)
                .consistencyRatio(config.getConsistencyRatio())
                .isConsistent(config.getIsConsistent())
                .description("AHP(Analytic Hierarchy Process) 기반 다기준 평가 가중치")
                .build();
    }

    /**
     * 잠재적 관리 대상(Watch List) 조회
     * - Red Zone에 근접한 사원 (Yellow Zone)
     * - 점수 하락세인 사원
     */
    public List<WatchListEmployeeDto> getWatchList() {
        List<Employee> employees = employeeRepository.findByIsEnabledTrue();
        SystemConfig config = configRepository.getDefaultConfig();
        String currentPeriod = YearMonth.now().toString();
        String previousPeriod = YearMonth.now().minusMonths(1).toString();

        // Yellow Zone 기준 (Red Zone 보다 조금 높은 임계치)
        BigDecimal hcroiYellow = new BigDecimal("1.2"); // Red: 1.0
        BigDecimal scoreYellow = new BigDecimal("750"); // Red: 700

        List<WatchListEmployeeDto> watchList = new ArrayList<>();

        for (Employee emp : employees) {
            // 현재 월 실적
            Optional<Employee.PerformanceLog> currentLogOpt = emp.getPerformanceLogs().stream()
                    .filter(l -> currentPeriod.equals(l.getPeriod()))
                    .findFirst();
            // 이전 월 실적
            Optional<Employee.PerformanceLog> prevLogOpt = emp.getPerformanceLogs().stream()
                    .filter(l -> previousPeriod.equals(l.getPeriod()))
                    .findFirst();

            if (!currentLogOpt.isPresent())
                continue;
            Employee.PerformanceLog currentLog = currentLogOpt.get();

            // HCROI 계산
            BigDecimal monthlySalary = emp.getCurrentSalary()
                    .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
            BigDecimal benefitCost = monthlySalary.multiply(config.getInsuranceRate());
            BigDecimal currentHcroi;
            try {
                HcroiCalculator.HcroiResult result = hcroiCalculator.calculateWithBenefits(
                        currentLog.getAchievedSales(), monthlySalary, benefitCost,
                        config.getFixedCostPerPerson(), config.getTargetProfitRate());
                currentHcroi = result.getHcroiIndex();
            } catch (Exception e) {
                continue;
            }

            // 현재 점수
            BigDecimal currentScore = emp.getScoreHistories().isEmpty() ? BigDecimal.ZERO
                    : emp.getScoreHistories().get(emp.getScoreHistories().size() - 1).getNewScore();

            // 이전 월 데이터
            BigDecimal prevHcroi = null;
            BigDecimal prevScore = null;
            if (prevLogOpt.isPresent()) {
                try {
                    HcroiCalculator.HcroiResult prevResult = hcroiCalculator.calculateWithBenefits(
                            prevLogOpt.get().getAchievedSales(), monthlySalary, benefitCost,
                            config.getFixedCostPerPerson(), config.getTargetProfitRate());
                    prevHcroi = prevResult.getHcroiIndex();
                } catch (Exception e) {
                    // skip
                }
            }

            // Watch List 조건 체크
            boolean isYellowZone = (currentHcroi.compareTo(hcroiYellow) < 0
                    && currentHcroi.compareTo(HCROI_THRESHOLD) >= 0)
                    || (currentScore.compareTo(scoreYellow) < 0 && currentScore.compareTo(SCORE_THRESHOLD) >= 0);
            boolean isDeclining = prevHcroi != null && currentHcroi.compareTo(prevHcroi) < 0;

            // Red Zone은 제외
            boolean isRedZone = currentHcroi.compareTo(HCROI_THRESHOLD) < 0
                    || currentScore.compareTo(SCORE_THRESHOLD) < 0;
            if (isRedZone)
                continue;

            if (isYellowZone || isDeclining) {
                String reason = "";
                String riskLevel = "YELLOW";
                if (isYellowZone && isDeclining) {
                    reason = "커트라인 근접 + 하락세";
                    riskLevel = "ORANGE";
                } else if (isYellowZone) {
                    reason = "커트라인 근접";
                } else {
                    reason = "성과 하락세";
                }

                BigDecimal hcroiChange = prevHcroi != null ? currentHcroi.subtract(prevHcroi) : null;
                BigDecimal distanceToRed = currentHcroi.subtract(HCROI_THRESHOLD);

                watchList.add(WatchListEmployeeDto.builder()
                        .employeeId(emp.getId())
                        .employeeName(emp.getName())
                        .currentGrade(emp.getCurrentGrade())
                        .currentHcroi(currentHcroi)
                        .currentScore(currentScore)
                        .previousHcroi(prevHcroi)
                        .previousScore(prevScore)
                        .hcroiChange(hcroiChange)
                        .scoreChange(null)
                        .watchReason(reason)
                        .riskLevel(riskLevel)
                        .distanceToRedZone(distanceToRed)
                        .build());
            }
        }

        // distanceToRedZone 기준 정렬 (가장 위험한 순)
        watchList.sort((a, b) -> a.getDistanceToRedZone().compareTo(b.getDistanceToRedZone()));

        // 최대 5명만 반환
        return watchList.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * 곧 다가오는 생일 조회 (30일 이내)
     */
    public List<UpcomingBirthdayDto> getUpcomingBirthdays() {
        List<Employee> employees = employeeRepository.findAll();
        java.time.LocalDate today = java.time.LocalDate.now();

        List<UpcomingBirthdayDto> upcomingBirthdays = new ArrayList<>();

        for (Employee emp : employees) {
            if (emp.getBirthDate() == null)
                continue;

            // 올해 생일 계산
            java.time.LocalDate thisYearBirthday = emp.getBirthDate().withYear(today.getYear());

            // 올해 생일이 이미 지났으면 내년 생일로
            if (thisYearBirthday.isBefore(today)) {
                thisYearBirthday = thisYearBirthday.plusYears(1);
            }

            // D-day 계산
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, thisYearBirthday);

            // 30일 이내인 경우만 포함
            if (daysUntil >= 0 && daysUntil <= 30) {
                String message = daysUntil == 0 ? "🎂 오늘!" : daysUntil == 1 ? "🎉 내일" : "D-" + daysUntil;

                upcomingBirthdays.add(UpcomingBirthdayDto.builder()
                        .employeeId(emp.getId())
                        .employeeName(emp.getName())
                        .currentGrade(emp.getCurrentGrade())
                        .birthDate(emp.getBirthDate())
                        .daysUntilBirthday((int) daysUntil)
                        .message(message)
                        .build());
            }
        }

        // D-day 기준 정렬 (가장 가까운 순)
        upcomingBirthdays.sort(Comparator.comparingInt(UpcomingBirthdayDto::getDaysUntilBirthday));

        // 최대 10명만 반환
        return upcomingBirthdays.stream().limit(10).collect(Collectors.toList());
    }
}
