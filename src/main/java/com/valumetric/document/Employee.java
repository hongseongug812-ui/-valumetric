package com.valumetric.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사원 Document (MongoDB)
 * 
 * <pre>
 * 복잡한 데이터를 내장(Embedded)하여 단일 문서로 관리:
 * - 월별 실적 (performanceLogs)
 * - 점수 변동 내역 (scoreHistories)
 * - 경고 기록 (alerts)
 * </pre>
 */
@Document(collection = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private Boolean isEnabled = true;

    private LocalDate hireDate;

    private LocalDate birthDate; // 생일

    private String currentGrade;

    private BigDecimal currentSalary;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    // ==================== 내장 데이터 ====================

    /**
     * 월별 실적 기록 (Embedded List)
     */
    @Builder.Default
    private List<PerformanceLog> performanceLogs = new ArrayList<>();

    /**
     * 점수 변동 내역 (Embedded List)
     */
    @Builder.Default
    private List<ScoreHistory> scoreHistories = new ArrayList<>();

    /**
     * 경고 기록 (Embedded List)
     */
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();

    // ==================== 내장 클래스 ====================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceLog {
        private String period; // "2024-01" 형식
        private BigDecimal targetSales;
        private BigDecimal achievedSales;
        private BigDecimal profit;
        private LocalDateTime recordedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreHistory {
        private String criteriaName;
        private BigDecimal previousScore;
        private BigDecimal scoreChange;
        private BigDecimal newScore;
        private String reason;
        private LocalDateTime changedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Alert {
        private AlertType alertType;
        private String message;
        private BigDecimal thresholdValue;
        private BigDecimal actualValue;
        @Builder.Default
        private Boolean isResolved = false;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;
    }

    public enum Role {
        USER,
        ADMIN
    }

    public enum AlertType {
        LOW_HCROI,
        LOW_SCORE,
        UNDERPERFORMANCE
    }
}
