import {
    LineChart, Line, XAxis, YAxis, CartesianGrid,
    Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import type { MonthlyTrendData } from '../../types';

interface PerformanceTrendChartProps {
    data: MonthlyTrendData[];
    title?: string;
}

const PerformanceTrendChart = ({ data, title = '6개월 추이' }: PerformanceTrendChartProps) => {
    return (
        <div className="card">
            <h3 className="text-lg font-semibold mb-4">{title}</h3>
            <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" className="opacity-30" />
                    <XAxis
                        dataKey="period"
                        tick={{ fontSize: 12 }}
                        tickFormatter={(value) => value.slice(5)} // "2024-01" -> "01"
                    />
                    <YAxis yAxisId="left" tick={{ fontSize: 12 }} />
                    <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 12 }} />
                    <Tooltip
                        contentStyle={{
                            backgroundColor: 'rgba(255, 255, 255, 0.95)',
                            borderRadius: '8px',
                            border: 'none',
                            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
                        }}
                    />
                    <Legend />
                    <Line
                        yAxisId="left"
                        type="monotone"
                        dataKey="revenue"
                        name="매출"
                        stroke="#3b82f6"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                    />
                    <Line
                        yAxisId="right"
                        type="monotone"
                        dataKey="score"
                        name="점수"
                        stroke="#10b981"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                    />
                    <Line
                        yAxisId="right"
                        type="monotone"
                        dataKey="hcroi"
                        name="HCROI"
                        stroke="#f59e0b"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
};

export default PerformanceTrendChart;
