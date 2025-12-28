package com.valumetric.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * HCROI(Human Capital Return on Investment) 계산기
 * 
 * Jac Fitz-enz의 인적자본 ROI 공식을 기반으로 구현
 * 
 * <pre>
 * 참고 문헌: "The ROI of Human Capital" by Jac Fitz-enz
 * </pre>
 */
public class HcroiCalculator {

    // 기본 소수점 자릿수
    private static final int DEFAULT_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * HCROI 계산 결과를 담는 DTO
     */
    public static class HcroiResult {
        private final BigDecimal breakEvenPointSales; // 손익분기점 매출액 (BEP)
        private final BigDecimal targetAchievementRate; // 목표 달성률 (%)
        private final BigDecimal hcroiIndex; // HCROI 지수

        public HcroiResult(BigDecimal breakEvenPointSales,
                BigDecimal targetAchievementRate,
                BigDecimal hcroiIndex) {
            this.breakEvenPointSales = breakEvenPointSales;
            this.targetAchievementRate = targetAchievementRate;
            this.hcroiIndex = hcroiIndex;
        }

        public BigDecimal getBreakEvenPointSales() {
            return breakEvenPointSales;
        }

        public BigDecimal getTargetAchievementRate() {
            return targetAchievementRate;
        }

        public BigDecimal getHcroiIndex() {
            return hcroiIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "HcroiResult{BEP=%s, 목표달성률=%s%%, HCROI=%s}",
                    breakEvenPointSales, targetAchievementRate, hcroiIndex);
        }
    }

    /**
     * HCROI 관련 지표들을 계산합니다.
     * 
     * @param revenue          매출액 (실제 달성 매출)
     * @param salary           연봉 (인건비, Human Capital Cost)
     * @param fixedCost        고정비 (인건비 외 운영 비용)
     * @param targetProfitRate 목표 이익률 (예: 0.15 = 15%)
     * @return HcroiResult 계산 결과 (BEP, 목표달성률, HCROI)
     */
    public HcroiResult calculate(BigDecimal revenue,
            BigDecimal salary,
            BigDecimal fixedCost,
            BigDecimal targetProfitRate) {

        validateInputs(revenue, salary, fixedCost, targetProfitRate);

        BigDecimal bep = calculateBreakEvenPoint(salary, fixedCost, targetProfitRate);
        BigDecimal achievementRate = calculateTargetAchievementRate(revenue, bep);
        // 복리후생비 미포함 시 간소화 버전 사용
        BigDecimal hcroi = calculateHcroiSimplified(revenue, salary, fixedCost);

        return new HcroiResult(bep, achievementRate, hcroi);
    }

    /**
     * HCROI 관련 지표들을 계산합니다. (복리후생비 포함 - Jac Fitz-enz 정식 공식)
     * 
     * @param revenue          매출액 (실제 달성 매출)
     * @param salary           연봉 (인건비)
     * @param benefitCost      복리후생비 (4대보험, 퇴직금 등)
     * @param fixedCost        고정비 (인건비 외 운영 비용)
     * @param targetProfitRate 목표 이익률 (예: 0.15 = 15%)
     * @return HcroiResult 계산 결과 (BEP, 목표달성률, HCROI)
     */
    public HcroiResult calculateWithBenefits(BigDecimal revenue,
            BigDecimal salary,
            BigDecimal benefitCost,
            BigDecimal fixedCost,
            BigDecimal targetProfitRate) {

        validateInputs(revenue, salary, fixedCost, targetProfitRate);
        if (benefitCost == null || benefitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("복리후생비는 null이거나 음수가 될 수 없습니다");
        }

        // BEP 계산 시 총 인건비 사용
        BigDecimal totalHumanCapitalCost = salary.add(benefitCost);
        BigDecimal bep = calculateBreakEvenPoint(totalHumanCapitalCost, fixedCost, targetProfitRate);
        BigDecimal achievementRate = calculateTargetAchievementRate(revenue, bep);
        BigDecimal hcroi = calculateHcroi(revenue, salary, benefitCost, fixedCost);

        return new HcroiResult(bep, achievementRate, hcroi);
    }

    /**
     * 손익분기점 매출액 (BEP: Break-Even Point) 계산
     * 
     * <pre>
     * 【공식】
     * BEP = (총 비용) / (1 - 목표 이익률)
     *     = (인건비 + 고정비) / (1 - 목표 이익률)
     * 
     * 【설명】
     * - 손익분기점: 수익과 비용이 같아지는 매출 수준
     * - 목표 이익률을 고려하면, 해당 이익률을 달성하기 위해 필요한 최소 매출액
     * - 예: 비용 100만원, 목표이익률 20% → BEP = 100 / 0.8 = 125만원
     * </pre>
     * 
     * @param salary           연봉 (인건비)
     * @param fixedCost        고정비
     * @param targetProfitRate 목표 이익률 (0~1 사이)
     * @return 손익분기점 매출액
     */
    public BigDecimal calculateBreakEvenPoint(BigDecimal salary,
            BigDecimal fixedCost,
            BigDecimal targetProfitRate) {
        // 총 비용 = 인건비 + 고정비
        BigDecimal totalCost = salary.add(fixedCost);

        // 분모 = (1 - 목표 이익률)
        // 목표 이익률이 1(100%)이면 분모가 0이 되어 계산 불가
        BigDecimal denominator = BigDecimal.ONE.subtract(targetProfitRate);

        if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("목표 이익률은 100% 미만이어야 합니다");
        }

        // BEP = 총 비용 / (1 - 목표 이익률)
        return totalCost.divide(denominator, DEFAULT_SCALE, ROUNDING_MODE);
    }

    /**
     * 목표 달성률 (%) 계산
     * 
     * <pre>
     * 【공식】
     * 목표 달성률 = (실제 매출액 / 손익분기점 매출액) × 100
     * 
     * 【해석】
     * - 100% 이상: 손익분기점 돌파, 이익 발생
     * - 100% 미만: 손익분기점 미달, 손실 발생
     * - 예: 실제매출 150만원, BEP 125만원 → 달성률 = 120%
     * </pre>
     * 
     * @param revenue 실제 달성 매출액
     * @param bep     손익분기점 매출액
     * @return 목표 달성률 (%)
     */
    public BigDecimal calculateTargetAchievementRate(BigDecimal revenue, BigDecimal bep) {
        if (bep.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("손익분기점 매출액은 0이 될 수 없습니다");
        }

        // 달성률 = (실제 매출 / BEP) × 100
        return revenue
                .divide(bep, DEFAULT_SCALE + 2, ROUNDING_MODE)
                .multiply(new BigDecimal("100"))
                .setScale(2, ROUNDING_MODE);
    }

    /**
     * HCROI (Human Capital Return on Investment) 지수 계산
     * 
     * <pre>
     * 【Jac Fitz-enz 원래 공식】
     * HCROI = (Revenue - (Operating Expenses - (Compensation + Benefits))) 
     *         / (Compensation + Benefits)
     * 
     * 【공식 풀이】
     * HCROI = (매출액 - (운영비용 - 총인건비)) / 총인건비
     *       = (매출액 - 비인건비 운영비용) / 총인건비
     * 
     * 【본 구현】
     * - 총인건비 = 연봉(Salary) + 복리후생비(4대보험 등)
     * - 비인건비 운영비용 = 고정비(FixedCost)
     * 
     * HCROI = (매출액 - 고정비) / (연봉 + 복리후생비)
     * 
     * 【해석】
     * - HCROI > 1: 인건비 1원당 1원 이상의 가치 창출
     * - HCROI = 1: 인건비만큼의 수익 (손익분기)
     * - HCROI < 1: 인건비 투자 대비 손실 발생
     * 
     * 【예시】
     * - 매출 500만원, 고정비 100만원, 연봉 200만원, 복리후생비 20만원
     * - HCROI = (500 - 100) / (200 + 20) = 400 / 220 = 1.818
     * - 해석: 인건비 1원당 약 1.82원의 가치 창출
     * </pre>
     * 
     * @param revenue     매출액
     * @param salary      연봉 (Regular Compensation Cost)
     * @param benefitCost 복리후생비 (Benefit Cost: 4대보험, 퇴직금 등)
     * @param fixedCost   고정비 (비인건비 운영비용)
     * @return HCROI 지수
     */
    public BigDecimal calculateHcroi(BigDecimal revenue,
            BigDecimal salary,
            BigDecimal benefitCost,
            BigDecimal fixedCost) {
        // 총 인건비 = 연봉 + 복리후생비
        BigDecimal totalHumanCapitalCost = salary.add(benefitCost);

        if (totalHumanCapitalCost.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("총 인건비(연봉 + 복리후생비)는 0이 될 수 없습니다");
        }

        // HCROI = (매출액 - 비인건비 운영비용) / 총인건비
        BigDecimal adjustedProfit = revenue.subtract(fixedCost);

        return adjustedProfit.divide(totalHumanCapitalCost, DEFAULT_SCALE, ROUNDING_MODE);
    }

    /**
     * HCROI 간소화 버전 (복리후생비 미포함 시 사용)
     * 
     * <pre>
     * 복리후생비를 별도로 계산하기 어려운 경우,
     * 연봉만으로 근사 계산을 수행합니다.
     * 
     * HCROI ≈ (매출액 - 고정비) / 연봉
     * </pre>
     * 
     * @param revenue   매출액
     * @param salary    연봉 (인건비)
     * @param fixedCost 고정비 (비인건비 운영비용)
     * @return HCROI 지수 (근사값)
     */
    public BigDecimal calculateHcroiSimplified(BigDecimal revenue,
            BigDecimal salary,
            BigDecimal fixedCost) {
        if (salary.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("연봉은 0이 될 수 없습니다");
        }

        BigDecimal adjustedProfit = revenue.subtract(fixedCost);

        return adjustedProfit.divide(salary, DEFAULT_SCALE, ROUNDING_MODE);
    }

    /**
     * 입력값 유효성 검증
     */
    private void validateInputs(BigDecimal revenue,
            BigDecimal salary,
            BigDecimal fixedCost,
            BigDecimal targetProfitRate) {
        if (revenue == null || salary == null || fixedCost == null || targetProfitRate == null) {
            throw new IllegalArgumentException("모든 입력값은 null이 될 수 없습니다");
        }

        if (revenue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("매출액은 음수가 될 수 없습니다");
        }

        if (salary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("인건비(연봉)는 0보다 커야 합니다");
        }

        if (fixedCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("고정비는 음수가 될 수 없습니다");
        }

        if (targetProfitRate.compareTo(BigDecimal.ZERO) < 0
                || targetProfitRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("목표 이익률은 0 이상, 1(100%) 미만이어야 합니다");
        }
    }
}
