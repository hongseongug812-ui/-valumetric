// Dashboard Types
export interface DashboardSummary {
    totalEmployeeCount: number;
    averageHcroi: number;
    averageScore: number;
    redZoneCount: number;
    unresolvedAlertCount: number;
    companyTotalRevenue: number;
}

export interface RedZoneEmployee {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    currentSalary: number;
    currentHcroi: number;
    currentScore: number;
    targetAchievementRate: number;
    riskLevel: 'WARNING' | 'CRITICAL';
    riskReason: string;
    unresolvedAlertCount: number;
}

export interface WatchListEmployee {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    currentHcroi: number;
    currentScore: number;
    previousHcroi: number | null;
    previousScore: number | null;
    hcroiChange: number | null;
    scoreChange: number | null;
    watchReason: string;
    riskLevel: 'YELLOW' | 'ORANGE';
    distanceToRedZone: number;
}

export interface DashboardResponse {
    summary: DashboardSummary;
    redZoneEmployees: RedZoneEmployee[];
    topPerformers: TopPerformer[];
    watchList: WatchListEmployee[];
}

export interface TopPerformer {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    currentSalary: number;
    currentHcroi: number;
    currentScore: number;
    targetAchievementRate: number;
    performanceLevel: 'EXCELLENT' | 'OUTSTANDING';
    achievement: string;
    consecutiveMonths: number;
}

export interface MonthlyTrendData {
    period: string;
    revenue: number;
    score: number;
    hcroi: number;
    achievementRate: number;
}

export interface EmployeeTrend {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    trendData: MonthlyTrendData[];
}

// Admin Types
export interface SalaryConfig {
    id: string;
    fixedCostPerPerson: number;
    insuranceRate: number;
    targetProfitRate: number;
}

export interface AhpWeightResponse {
    weights: number[];
    criteriaNames: string[];
    lambdaMax: number;
    consistencyIndex: number;
    consistencyRatio: number;
    isConsistent: boolean;
    message: string;
}

