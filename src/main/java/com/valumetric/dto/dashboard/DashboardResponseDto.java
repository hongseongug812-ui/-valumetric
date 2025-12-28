package com.valumetric.dto.dashboard;

import lombok.*;

import java.util.List;

/**
 * 대시보드 전체 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDto {

    private DashboardSummaryDto summary;
    private List<RedZoneEmployeeDto> redZoneEmployees;
    private List<TopPerformerDto> topPerformers;
    private List<WatchListEmployeeDto> watchList;
}
