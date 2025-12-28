import { useEffect, useState } from 'react';
import api from '../../api';

interface BepStatus {
    period: string;
    targetRevenue: number;
    currentRevenue: number;
    bepRevenue: number;
    achievementRate: number;
    bepAchievementRate: number;
    remainingToBep: number;
    remainingToTarget: number;
    bepAchieved: boolean;
    targetAchieved: boolean;
    contributingEmployees: number;
}

export default function BepGauge() {
    const [data, setData] = useState<BepStatus | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadBepStatus();
    }, []);

    const loadBepStatus = async () => {
        try {
            const res = await api.get('/dashboard/bep-status');
            setData(res.data);
        } catch (error) {
            console.error('BEP 데이터 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    🎯 손익분기점 달성 현황
                </h2>
                <div className="h-32 flex items-center justify-center text-gray-500">
                    로딩 중...
                </div>
            </div>
        );
    }

    if (!data || data.bepRevenue === 0) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    🎯 손익분기점 달성 현황
                </h2>
                <div className="h-32 flex items-center justify-center text-gray-500">
                    실적 데이터가 없습니다.
                </div>
            </div>
        );
    }

    // 프로그레스 바 계산
    const maxValue = Math.max(data.targetRevenue, data.currentRevenue, data.bepRevenue) * 1.1;
    const currentPercent = (data.currentRevenue / maxValue) * 100;
    const bepPercent = (data.bepRevenue / maxValue) * 100;
    const targetPercent = (data.targetRevenue / maxValue) * 100;

    // 색상 결정
    const barColor = data.bepAchieved
        ? data.targetAchieved ? 'from-green-400 to-green-600' : 'from-green-400 to-yellow-500'
        : 'from-red-400 to-red-600';

    const formatMillion = (value: number) => {
        if (value >= 100000000) return `${(value / 100000000).toFixed(1)}억`;
        if (value >= 10000) return `${Math.round(value / 10000)}만`;
        return value.toLocaleString();
    };

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
            <div className="flex justify-between items-start mb-4">
                <div>
                    <h2 className="text-lg font-bold text-gray-800 dark:text-white">
                        🎯 손익분기점 달성 현황
                    </h2>
                    <p className="text-sm text-gray-500">{data.period} | {data.contributingEmployees}명 기여</p>
                </div>
                <div className={`px-3 py-1 rounded-full text-sm font-bold ${data.bepAchieved
                        ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                        : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                    }`}>
                    {data.bepAchieved ? '✅ BEP 달성' : '⚠️ BEP 미달'}
                </div>
            </div>

            {/* 메인 프로그레스 바 */}
            <div className="relative mb-6">
                {/* 배경 */}
                <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                    {/* 현재 진행 바 */}
                    <div
                        className={`h-full bg-gradient-to-r ${barColor} rounded-full transition-all duration-1000 ease-out flex items-center justify-end pr-2`}
                        style={{ width: `${Math.min(currentPercent, 100)}%` }}
                    >
                        <span className="text-white text-xs font-bold drop-shadow">
                            {formatMillion(data.currentRevenue)}
                        </span>
                    </div>
                </div>

                {/* BEP 마커 */}
                <div
                    className="absolute top-0 h-8 flex flex-col items-center"
                    style={{ left: `${bepPercent}%`, transform: 'translateX(-50%)' }}
                >
                    <div className="w-1 h-8 bg-orange-500"></div>
                    <div className="absolute -top-6 text-xs text-orange-600 dark:text-orange-400 font-medium whitespace-nowrap">
                        BEP ({formatMillion(data.bepRevenue)})
                    </div>
                </div>

                {/* 목표 마커 */}
                <div
                    className="absolute top-0 h-8 flex flex-col items-center"
                    style={{ left: `${targetPercent}%`, transform: 'translateX(-50%)' }}
                >
                    <div className="w-1 h-8 bg-blue-500"></div>
                    <div className="absolute -bottom-5 text-xs text-blue-600 dark:text-blue-400 font-medium whitespace-nowrap">
                        목표 ({formatMillion(data.targetRevenue)})
                    </div>
                </div>
            </div>

            {/* 상세 정보 */}
            <div className="grid grid-cols-3 gap-4 mt-8">
                <div className="text-center">
                    <p className="text-sm text-gray-500 mb-1">BEP 달성률</p>
                    <p className={`text-2xl font-bold ${data.bepAchievementRate >= 100 ? 'text-green-600' : 'text-red-600'
                        }`}>
                        {data.bepAchievementRate?.toFixed(1)}%
                    </p>
                </div>
                <div className="text-center">
                    <p className="text-sm text-gray-500 mb-1">목표 달성률</p>
                    <p className={`text-2xl font-bold ${data.achievementRate >= 100 ? 'text-green-600' :
                            data.achievementRate >= 80 ? 'text-yellow-600' : 'text-red-600'
                        }`}>
                        {data.achievementRate?.toFixed(1)}%
                    </p>
                </div>
                <div className="text-center">
                    <p className="text-sm text-gray-500 mb-1">
                        {data.bepAchieved ? 'BEP 초과 달성' : 'BEP까지 남은 금액'}
                    </p>
                    <p className={`text-2xl font-bold ${data.bepAchieved ? 'text-green-600' : 'text-red-600'
                        }`}>
                        {data.bepAchieved ? '+' : ''}{formatMillion(Math.abs(data.remainingToBep))}
                    </p>
                </div>
            </div>

            {/* 경고 메시지 */}
            {!data.bepAchieved && (
                <div className="mt-4 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-200 dark:border-red-800">
                    <p className="text-sm text-red-700 dark:text-red-400">
                        ⚠️ 손익분기점까지 <strong>{formatMillion(data.remainingToBep)}원</strong>이 더 필요합니다.
                    </p>
                </div>
            )}

            {data.bepAchieved && !data.targetAchieved && (
                <div className="mt-4 p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg border border-yellow-200 dark:border-yellow-800">
                    <p className="text-sm text-yellow-700 dark:text-yellow-400">
                        📊 목표 달성까지 <strong>{formatMillion(data.remainingToTarget)}원</strong>이 남았습니다.
                    </p>
                </div>
            )}

            {data.targetAchieved && (
                <div className="mt-4 p-3 bg-green-50 dark:bg-green-900/20 rounded-lg border border-green-200 dark:border-green-800">
                    <p className="text-sm text-green-700 dark:text-green-400">
                        🎉 목표를 <strong>{formatMillion(Math.abs(data.remainingToTarget))}원</strong> 초과 달성했습니다!
                    </p>
                </div>
            )}
        </div>
    );
}
