import { useEffect, useState } from 'react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer
} from 'recharts';
import api from '../../api';

interface MonthlyData {
    period: string;
    totalRevenue: number;
    totalLaborCost: number;
    averageHcroi: number;
    employeeCount: number;
}

export default function RevenueLaborTrendChart() {
    const [data, setData] = useState<MonthlyData[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadTrendData();
    }, []);

    const loadTrendData = async () => {
        try {
            const res = await api.get('/dashboard/monthly-trend');
            setData(res.data.data || []);
        } catch (error) {
            console.error('추이 데이터 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    // 차트용 데이터 포맷
    const chartData = data.map(d => ({
        period: d.period.replace('2024-', '').replace('2025-', ''), // "07", "08" 등
        매출: Math.round(d.totalRevenue / 10000), // 만원 단위
        인건비: Math.round(d.totalLaborCost / 10000),
        HCROI: d.averageHcroi
    }));

    if (loading) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    📈 월별 매출 vs 인건비 추이
                </h2>
                <div className="h-64 flex items-center justify-center text-gray-500">
                    로딩 중...
                </div>
            </div>
        );
    }

    if (data.length === 0) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    📈 월별 매출 vs 인건비 추이
                </h2>
                <div className="h-64 flex items-center justify-center text-gray-500">
                    데이터가 없습니다. 실적을 입력해주세요.
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white">
                    📈 월별 매출 vs 인건비 추이
                </h2>
                <p className="text-sm text-gray-500">
                    매출 증가 & 인건비 유지 = HCROI 개선
                </p>
            </div>

            <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                        <XAxis
                            dataKey="period"
                            tick={{ fontSize: 12 }}
                            tickFormatter={(value) => `${value}월`}
                        />
                        <YAxis
                            tick={{ fontSize: 12 }}
                            tickFormatter={(value) => `${value.toLocaleString()}만`}
                        />
                        <Tooltip
                            formatter={(value: number, name: string) => [
                                name === 'HCROI' ? value.toFixed(2) : `${value.toLocaleString()}만원`,
                                name
                            ]}
                            labelFormatter={(label) => `${label}월`}
                        />
                        <Legend />
                        <Line
                            type="monotone"
                            dataKey="매출"
                            stroke="#10B981"
                            strokeWidth={3}
                            dot={{ fill: '#10B981', strokeWidth: 2 }}
                            activeDot={{ r: 8 }}
                        />
                        <Line
                            type="monotone"
                            dataKey="인건비"
                            stroke="#F59E0B"
                            strokeWidth={3}
                            dot={{ fill: '#F59E0B', strokeWidth: 2 }}
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>

            {/* HCROI 추이 미니 차트 */}
            <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">평균 HCROI 추이</span>
                    <div className="flex gap-4">
                        {chartData.map((d, i) => (
                            <div key={i} className="text-center">
                                <div className={`text-sm font-bold ${d.HCROI >= 1.5 ? 'text-green-600' :
                                        d.HCROI >= 1.0 ? 'text-blue-600' : 'text-red-600'
                                    }`}>
                                    {d.HCROI?.toFixed(2) || '-'}
                                </div>
                                <div className="text-xs text-gray-400">{d.period}월</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
