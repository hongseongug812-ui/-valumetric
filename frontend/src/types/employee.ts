// Employee Detail Types
export interface EmployeeDetail {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    currentSalary: number;
    hireDate: string;

    // HCROI 관련
    currentHcroi: number;
    targetAchievementRate: number;
    breakEvenPointSales: number;    // 손익분기점 매출액
    currentRevenue: number;          // 현재 매출
    remainingToBreakEven: number;    // 손익분기점까지 남은 매출

    // 점수 관련
    currentScore: number;
    scoreGrade: string;

    // 경고 관련
    unresolvedAlertCount: number;
    riskLevel?: 'WARNING' | 'CRITICAL';
}
