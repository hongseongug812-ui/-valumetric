package com.valumetric.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HcroiCalculator 단위 테스트
 * 
 * HCROI(Human Capital Return on Investment) 계산 로직을 검증합니다.
 * 
 * <pre>
 * 【Fitz-enz 공식】
 * HCROI = (Revenue - Operating Expenses) / Total Personnel Costs
 *       = (매출액 - 비인건비 비용) / 총인건비
 * </pre>
 */
@DisplayName("HcroiCalculator 단위 테스트")
class HcroiCalculatorTest {

    private HcroiCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HcroiCalculator();
    }

    @Nested
    @DisplayName("정상 케이스")
    class NormalCases {

        @Test
        @DisplayName("매출 1억, 비용 8천만, 인건비 1천만일 때 HCROI = 2.0")
        void calculateHcroi_standardCase() {
            // Given
            // HCROI = (매출 - 비인건비 비용) / 인건비
            // HCROI = (100,000,000 - 80,000,000) / 10,000,000
            // HCROI = 20,000,000 / 10,000,000 = 2.0
            BigDecimal revenue = new BigDecimal("100000000"); // 1억
            BigDecimal salary = new BigDecimal("10000000"); // 1천만 (인건비)
            BigDecimal fixedCost = new BigDecimal("80000000"); // 8천만 (비인건비 비용)
            BigDecimal targetProfitRate = new BigDecimal("0.15"); // 15%

            // When
            HcroiCalculator.HcroiResult result = calculator.calculate(
                    revenue, salary, fixedCost, targetProfitRate);

            // Then
            BigDecimal expectedHcroi = new BigDecimal("2.0000");
            assertEquals(0, expectedHcroi.compareTo(result.getHcroiIndex()),
                    "HCROI는 2.0이어야 합니다 (실제: " + result.getHcroiIndex() + ")");
        }

        @Test
        @DisplayName("매출이 손익분기점 미달일 때 HCROI < 1.0")
        void calculateHcroi_belowBreakeven() {
            // Given
            // 매출이 비용(인건비+고정비)보다 작으면 HCROI < 1.0
            BigDecimal revenue = new BigDecimal("50000000"); // 5천만
            BigDecimal salary = new BigDecimal("30000000"); // 3천만
            BigDecimal fixedCost = new BigDecimal("40000000"); // 4천만
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // HCROI = (50,000,000 - 40,000,000) / 30,000,000
            // = 10,000,000 / 30,000,000 = 0.3333...

            // When
            HcroiCalculator.HcroiResult result = calculator.calculate(
                    revenue, salary, fixedCost, targetProfitRate);

            // Then
            assertTrue(result.getHcroiIndex().compareTo(BigDecimal.ONE) < 0,
                    "손익분기점 미달 시 HCROI는 1.0 미만이어야 합니다 (실제: " + result.getHcroiIndex() + ")");
        }

        @Test
        @DisplayName("복리후생비 포함 HCROI 계산 - Jac Fitz-enz 정식 공식")
        void calculateHcroi_withBenefits() {
            // Given
            BigDecimal revenue = new BigDecimal("100000000"); // 1억
            BigDecimal salary = new BigDecimal("8000000"); // 8백만
            BigDecimal benefitCost = new BigDecimal("2000000"); // 2백만 (복리후생비)
            BigDecimal fixedCost = new BigDecimal("70000000"); // 7천만
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // HCROI = (100,000,000 - 70,000,000) / (8,000,000 + 2,000,000)
            // = 30,000,000 / 10,000,000 = 3.0

            // When
            HcroiCalculator.HcroiResult result = calculator.calculateWithBenefits(
                    revenue, salary, benefitCost, fixedCost, targetProfitRate);

            // Then
            BigDecimal expectedHcroi = new BigDecimal("3.0000");
            assertEquals(0, expectedHcroi.compareTo(result.getHcroiIndex()),
                    "HCROI는 3.0이어야 합니다 (실제: " + result.getHcroiIndex() + ")");
        }

        @Test
        @DisplayName("손익분기점(BEP) 계산 검증")
        void calculateBreakEvenPoint() {
            // Given
            BigDecimal revenue = new BigDecimal("100000000");
            BigDecimal salary = new BigDecimal("10000000");
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("0.20"); // 20%

            // BEP = (인건비 + 고정비) / (1 - 목표이익률)
            // = (10,000,000 + 50,000,000) / (1 - 0.20)
            // = 60,000,000 / 0.80 = 75,000,000

            // When
            HcroiCalculator.HcroiResult result = calculator.calculate(
                    revenue, salary, fixedCost, targetProfitRate);

            // Then
            BigDecimal expectedBep = new BigDecimal("75000000.0000");
            assertEquals(0, expectedBep.compareTo(result.getBreakEvenPointSales()),
                    "BEP는 7500만이어야 합니다 (실제: " + result.getBreakEvenPointSales() + ")");
        }

        @Test
        @DisplayName("목표 달성률 계산 검증")
        void calculateTargetAchievementRate() {
            // Given
            BigDecimal revenue = new BigDecimal("90000000"); // 실제 매출 9천만
            BigDecimal salary = new BigDecimal("10000000");
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("0.20");

            // BEP = 75,000,000
            // 달성률 = (90,000,000 / 75,000,000) * 100 = 120%

            // When
            HcroiCalculator.HcroiResult result = calculator.calculate(
                    revenue, salary, fixedCost, targetProfitRate);

            // Then
            BigDecimal expectedRate = new BigDecimal("120.0000");
            assertEquals(0, expectedRate.compareTo(result.getTargetAchievementRate()),
                    "달성률은 120%여야 합니다 (실제: " + result.getTargetAchievementRate() + ")");
        }
    }

    @Nested
    @DisplayName("예외 및 경계 케이스")
    class EdgeCases {

        @Test
        @DisplayName("매출이 0일 때 HCROI 계산")
        void calculateHcroi_zeroRevenue() {
            // Given
            BigDecimal revenue = BigDecimal.ZERO;
            BigDecimal salary = new BigDecimal("10000000");
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // When
            HcroiCalculator.HcroiResult result = calculator.calculate(
                    revenue, salary, fixedCost, targetProfitRate);

            // Then
            // 매출 0 - 고정비용 / 인건비 = 음수 HCROI
            // (0 - 50,000,000) / 10,000,000 = -5.0
            assertTrue(result.getHcroiIndex().compareTo(BigDecimal.ZERO) < 0,
                    "매출 0일 때 HCROI는 음수여야 합니다 (실제: " + result.getHcroiIndex() + ")");
        }

        @Test
        @DisplayName("인건비가 0일 때 ArithmeticException 발생")
        void calculateHcroi_zeroSalary_throwsException() {
            // Given
            BigDecimal revenue = new BigDecimal("100000000");
            BigDecimal salary = BigDecimal.ZERO; // 0원
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                calculator.calculate(revenue, salary, fixedCost, targetProfitRate);
            }, "인건비가 0일 때는 IllegalArgumentException이 발생해야 합니다");
        }

        @Test
        @DisplayName("음수 매출은 IllegalArgumentException 발생")
        void calculateHcroi_negativeRevenue_throwsException() {
            // Given
            BigDecimal revenue = new BigDecimal("-100000000");
            BigDecimal salary = new BigDecimal("10000000");
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                calculator.calculate(revenue, salary, fixedCost, targetProfitRate);
            }, "음수 매출은 IllegalArgumentException이 발생해야 합니다");
        }

        @Test
        @DisplayName("null 파라미터는 IllegalArgumentException 발생")
        void calculateHcroi_nullParameter_throwsException() {
            // Given
            BigDecimal salary = new BigDecimal("10000000");
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                calculator.calculate(null, salary, fixedCost, targetProfitRate);
            }, "null 매출은 IllegalArgumentException이 발생해야 합니다");
        }

        @Test
        @DisplayName("목표이익률이 100% 이상일 때 예외 발생")
        void calculateHcroi_invalidTargetProfitRate_throwsException() {
            // Given
            BigDecimal revenue = new BigDecimal("100000000");
            BigDecimal salary = new BigDecimal("10000000");
            BigDecimal fixedCost = new BigDecimal("50000000");
            BigDecimal targetProfitRate = new BigDecimal("1.0"); // 100%

            // When & Then
            // BEP 계산 시 (1 - 1.0) = 0 으로 나누기 에러
            assertThrows(IllegalArgumentException.class, () -> {
                calculator.calculate(revenue, salary, fixedCost, targetProfitRate);
            }, "목표이익률 100%는 IllegalArgumentException이 발생해야 합니다");
        }
    }

    @Nested
    @DisplayName("정밀도 테스트")
    class PrecisionTests {

        @Test
        @DisplayName("소수점 4자리까지 정확하게 계산")
        void calculateHcroi_precision() {
            // Given
            BigDecimal revenue = new BigDecimal("100000000");
            BigDecimal salary = new BigDecimal("30000000"); // 3천만
            BigDecimal fixedCost = new BigDecimal("60000000"); // 6천만
            BigDecimal targetProfitRate = new BigDecimal("0.15");

            // HCROI = (100,000,000 - 60,000,000) / 30,000,000
            // = 40,000,000 / 30,000,000 = 1.3333...

            // When
            HcroiCalculator.HcroiResult result = calculator.calculate(
                    revenue, salary, fixedCost, targetProfitRate);

            // Then
            BigDecimal expectedHcroi = new BigDecimal("1.3333");
            assertEquals(expectedHcroi.setScale(4, RoundingMode.HALF_UP),
                    result.getHcroiIndex().setScale(4, RoundingMode.HALF_UP),
                    "HCROI는 소수점 4자리까지 1.3333이어야 합니다");
        }
    }
}
