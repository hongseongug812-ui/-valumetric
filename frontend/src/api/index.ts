import axios from 'axios';
import type {
    DashboardResponse,
    DashboardSummary,
    RedZoneEmployee,
    EmployeeTrend,
    SalaryConfig,
    AhpWeightResponse
} from '../types';

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Dashboard API
export const dashboardApi = {
    getDashboard: () =>
        api.get<DashboardResponse>('/dashboard'),

    getSummary: () =>
        api.get<DashboardSummary>('/dashboard/summary'),

    getRedZoneEmployees: () =>
        api.get<RedZoneEmployee[]>('/dashboard/red-zone'),

    getEmployeeTrend: (employeeId: string) =>
        api.get<EmployeeTrend>(`/dashboard/trend/${employeeId}`)
};

// Admin API
export const adminApi = {
    getSalaryConfig: () =>
        api.get<SalaryConfig>('/admin/salary-config'),

    updateSalaryConfig: (config: Partial<SalaryConfig>) =>
        api.put<SalaryConfig>('/admin/salary-config', config),

    getAhpWeights: () =>
        api.get<AhpWeightResponse>('/admin/ahp/weights'),

    calculateAhpWeights: (data: { matrixSize: number; upperTriangleValues: number[]; criteriaNames?: string[] }) =>
        api.post<AhpWeightResponse>('/admin/ahp/calculate', data),

    setAhpWeights: (data: { criteriaNames: string[]; weights: number[] }) =>
        api.put<AhpWeightResponse>('/admin/ahp/weights', data)
};

export default api;
