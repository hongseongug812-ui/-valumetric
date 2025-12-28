package com.valumetric.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 다가오는 생일 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingBirthdayDto {
    private String employeeId;
    private String employeeName;
    private String currentGrade;
    private LocalDate birthDate;
    private int daysUntilBirthday; // D-day (0=오늘, 1=내일)
    private String message; // "오늘", "D-3" 등
}
