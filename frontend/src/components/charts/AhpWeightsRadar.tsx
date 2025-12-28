import { useEffect, useState } from 'react';
import {
    RadarChart,
    PolarGrid,
    PolarAngleAxis,
    PolarRadiusAxis,
    Radar,
    ResponsiveContainer,
    Tooltip
} from 'recharts';
import api from '../../api';

interface CriteriaWeight {
    name: string;
    description: string;
    weight: number;
    percentage: number;
    displayOrder: number;
}

interface AhpWeights {
    criteria: CriteriaWeight[];
    consistencyRatio: number;
    isConsistent: boolean;
    description: string;
}

export default function AhpWeightsRadar() {
    const [data, setData] = useState<AhpWeights | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadAhpWeights();
    }, []);

    const loadAhpWeights = async () => {
        try {
            const res = await api.get('/dashboard/ahp-weights');
            setData(res.data);
        } catch (error) {
            console.error('AHP 가중치 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    ⚖️ AHP 평가 가중치
                </h2>
                <div className="h-64 flex items-center justify-center text-gray-500">
                    로딩 중...
                </div>
            </div>
        );
    }

    if (!data || !data.criteria || data.criteria.length === 0) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    ⚖️ AHP 평가 가중치
                </h2>
                <div className="h-64 flex items-center justify-center text-gray-500">
                    가중치 데이터가 없습니다.
                </div>
            </div>
        );
    }

    // Radar 차트용 데이터
    const chartData = data.criteria.map(c => ({
        criteria: c.name,
        weight: c.percentage,
        fullMark: 100
    }));

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
            <div className="flex justify-between items-start mb-2">
                <div>
                    <h2 className="text-lg font-bold text-gray-800 dark:text-white">
                        ⚖️ AHP 평가 가중치
                    </h2>
                    <p className="text-xs text-gray-500 mt-1">
                        Analytic Hierarchy Process 기반 다기준 의사결정
                    </p>
                </div>
                <div className={`px-2 py-1 rounded text-xs font-medium ${data.isConsistent
                        ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                        : 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400'
                    }`}>
                    CR: {(data.consistencyRatio * 100).toFixed(1)}%
                    {data.isConsistent ? ' ✓' : ' ⚠️'}
                </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
                {/* Radar Chart */}
                <div className="h-56">
                    <ResponsiveContainer width="100%" height="100%">
                        <RadarChart data={chartData} cx="50%" cy="50%" outerRadius="70%">
                            <PolarGrid stroke="#e5e7eb" />
                            <PolarAngleAxis
                                dataKey="criteria"
                                tick={{ fontSize: 11, fill: '#6b7280' }}
                            />
                            <PolarRadiusAxis
                                angle={90}
                                domain={[0, 100]}
                                tick={{ fontSize: 10 }}
                                tickCount={5}
                            />
                            <Radar
                                name="가중치"
                                dataKey="weight"
                                stroke="#3B82F6"
                                fill="#3B82F6"
                                fillOpacity={0.5}
                                strokeWidth={2}
                            />
                            <Tooltip
                                formatter={(value: number) => [`${value}%`, '가중치']}
                            />
                        </RadarChart>
                    </ResponsiveContainer>
                </div>

                {/* 가중치 목록 */}
                <div className="space-y-3">
                    <p className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        평가 기준별 가중치
                    </p>
                    {data.criteria.map((c, i) => (
                        <div key={i} className="space-y-1">
                            <div className="flex justify-between text-sm">
                                <span className="text-gray-700 dark:text-gray-300">{c.name}</span>
                                <span className="font-bold text-blue-600">{c.percentage}%</span>
                            </div>
                            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                                <div
                                    className="h-full bg-gradient-to-r from-blue-400 to-blue-600 rounded-full transition-all duration-500"
                                    style={{ width: `${c.percentage}%` }}
                                />
                            </div>
                            <p className="text-xs text-gray-400">{c.description}</p>
                        </div>
                    ))}
                </div>
            </div>

            {/* 설명 */}
            <div className="mt-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
                <p className="text-xs text-blue-700 dark:text-blue-400">
                    💡 <strong>점수 계산:</strong> 각 평가 항목의 점수에 위 가중치를 적용하여 최종 점수를 산출합니다.
                    예) 매출 80점 × 50% + 근태 90점 × 30% + 기타 85점 × 20% = <strong>84점</strong>
                </p>
            </div>
        </div>
    );
}
