import { useEffect } from 'react';
import type { EmployeeDetail } from '../../types/employee';
import type { MonthlyTrendData } from '../../types';
import PerformanceTrendChart from '../charts/PerformanceTrendChart';

interface EmployeeDetailModalProps {
    employee: EmployeeDetail | null;
    trendData?: MonthlyTrendData[];
    isOpen: boolean;
    onClose: () => void;
}

const EmployeeDetailModal = ({ employee, trendData, isOpen, onClose }: EmployeeDetailModalProps) => {
    // ESC 키로 닫기
    useEffect(() => {
        const handleEsc = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };
        if (isOpen) {
            document.addEventListener('keydown', handleEsc);
            document.body.style.overflow = 'hidden';
        }
        return () => {
            document.removeEventListener('keydown', handleEsc);
            document.body.style.overflow = 'unset';
        };
    }, [isOpen, onClose]);

    if (!isOpen || !employee) return null;

    // 손익분기점 진행률 계산
    const progressPercent = Math.min(
        (employee.currentRevenue / employee.breakEvenPointSales) * 100,
        100
    );
    const isBreakEvenReached = employee.currentRevenue >= employee.breakEvenPointSales;
    const remainingAmount = Math.max(0, employee.breakEvenPointSales - employee.currentRevenue);

    // 금액 포맷팅
    const formatCurrency = (value: number) => {
        return new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: 'KRW',
            maximumFractionDigits: 0
        }).format(value);
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm"
                onClick={onClose}
            />

            {/* Modal */}
            <div className="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto mx-4 animate-in fade-in zoom-in duration-200">
                {/* Header */}
                <div className="sticky top-0 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-6 py-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <span className="text-3xl">👤</span>
                        <div>
                            <h2 className="text-xl font-bold">{employee.employeeName}</h2>
                            <p className="text-sm text-gray-500">
                                {employee.currentGrade}등급 · {formatCurrency(employee.currentSalary)}/년
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 space-y-6">
                    {/* 위험 상태 배너 */}
                    {employee.riskLevel && (
                        <div className={`p-4 rounded-lg flex items-center gap-3 ${employee.riskLevel === 'CRITICAL'
                                ? 'bg-red-50 border border-red-200 text-red-800 dark:bg-red-900/30 dark:border-red-800 dark:text-red-200'
                                : 'bg-yellow-50 border border-yellow-200 text-yellow-800 dark:bg-yellow-900/30 dark:border-yellow-800 dark:text-yellow-200'
                            }`}>
                            <span className="text-2xl">{employee.riskLevel === 'CRITICAL' ? '🚨' : '⚠️'}</span>
                            <div>
                                <p className="font-semibold">
                                    {employee.riskLevel === 'CRITICAL' ? '긴급 주의 필요' : '주의 필요'}
                                </p>
                                <p className="text-sm opacity-80">
                                    미해결 경고 {employee.unresolvedAlertCount}건
                                </p>
                            </div>
                        </div>
                    )}

                    {/* 손익분기점 Progress Bar */}
                    <div className="bg-gray-50 dark:bg-gray-900 rounded-xl p-6">
                        <div className="flex items-center justify-between mb-3">
                            <h3 className="font-semibold flex items-center gap-2">
                                <span>📊</span>
                                손익분기점(BEP) 달성 현황
                            </h3>
                            <span className={`text-sm font-medium px-3 py-1 rounded-full ${isBreakEvenReached
                                    ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300'
                                    : 'bg-orange-100 text-orange-700 dark:bg-orange-900 dark:text-orange-300'
                                }`}>
                                {isBreakEvenReached ? '✓ 달성' : `${progressPercent.toFixed(1)}% 달성`}
                            </span>
                        </div>

                        {/* Progress Bar */}
                        <div className="relative h-8 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden mb-4">
                            <div
                                className={`absolute inset-y-0 left-0 rounded-full transition-all duration-500 ${isBreakEvenReached
                                        ? 'bg-gradient-to-r from-green-400 to-green-600'
                                        : progressPercent >= 80
                                            ? 'bg-gradient-to-r from-yellow-400 to-yellow-600'
                                            : 'bg-gradient-to-r from-blue-400 to-blue-600'
                                    }`}
                                style={{ width: `${progressPercent}%` }}
                            />
                            {/* 손익분기점 마커 */}
                            <div className="absolute inset-y-0 right-0 w-1 bg-red-500" title="손익분기점" />
                            {/* 현재 위치 표시 */}
                            <div
                                className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 bg-white dark:bg-gray-800 rounded-full px-2 py-0.5 text-xs font-bold shadow"
                                style={{ left: `${Math.min(progressPercent, 95)}%` }}
                            >
                                {progressPercent.toFixed(0)}%
                            </div>
                        </div>

                        {/* 상세 수치 */}
                        <div className="grid grid-cols-3 gap-4 text-center">
                            <div className="bg-white dark:bg-gray-800 rounded-lg p-3">
                                <p className="text-xs text-gray-500 mb-1">현재 매출</p>
                                <p className="text-lg font-bold text-blue-600">
                                    {formatCurrency(employee.currentRevenue)}
                                </p>
                            </div>
                            <div className="bg-white dark:bg-gray-800 rounded-lg p-3">
                                <p className="text-xs text-gray-500 mb-1">손익분기점</p>
                                <p className="text-lg font-bold text-gray-700 dark:text-gray-300">
                                    {formatCurrency(employee.breakEvenPointSales)}
                                </p>
                            </div>
                            <div className="bg-white dark:bg-gray-800 rounded-lg p-3">
                                <p className="text-xs text-gray-500 mb-1">남은 금액</p>
                                <p className={`text-lg font-bold ${isBreakEvenReached ? 'text-green-600' : 'text-red-600'
                                    }`}>
                                    {isBreakEvenReached ? '달성 ✓' : formatCurrency(remainingAmount)}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* HCROI & 점수 카드 */}
                    <div className="grid grid-cols-2 gap-4">
                        <div className="bg-gray-50 dark:bg-gray-900 rounded-xl p-5">
                            <p className="text-sm text-gray-500 mb-2">HCROI 지수</p>
                            <p className={`text-3xl font-bold ${employee.currentHcroi >= 1 ? 'text-green-600' : 'text-red-600'
                                }`}>
                                {employee.currentHcroi.toFixed(4)}
                            </p>
                            <p className="text-xs text-gray-400 mt-1">
                                기준: 1.0 이상
                            </p>
                        </div>
                        <div className="bg-gray-50 dark:bg-gray-900 rounded-xl p-5">
                            <p className="text-sm text-gray-500 mb-2">현재 점수</p>
                            <div className="flex items-baseline gap-2">
                                <p className={`text-3xl font-bold ${employee.currentScore >= 700 ? 'text-green-600' : 'text-red-600'
                                    }`}>
                                    {employee.currentScore.toFixed(0)}
                                </p>
                                <span className={`text-lg font-medium px-2 py-0.5 rounded ${employee.scoreGrade === 'S' ? 'bg-purple-100 text-purple-700' :
                                        employee.scoreGrade === 'A' ? 'bg-blue-100 text-blue-700' :
                                            employee.scoreGrade === 'B' ? 'bg-green-100 text-green-700' :
                                                employee.scoreGrade === 'C' ? 'bg-yellow-100 text-yellow-700' :
                                                    'bg-red-100 text-red-700'
                                    }`}>
                                    {employee.scoreGrade}
                                </span>
                            </div>
                            <p className="text-xs text-gray-400 mt-1">
                                달성률: {employee.targetAchievementRate.toFixed(1)}%
                            </p>
                        </div>
                    </div>

                    {/* 6개월 추이 차트 */}
                    {trendData && trendData.length > 0 && (
                        <div>
                            <h3 className="font-semibold mb-4 flex items-center gap-2">
                                <span>📈</span>
                                6개월 성과 추이
                            </h3>
                            <PerformanceTrendChart data={trendData} />
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="sticky bottom-0 bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700 px-6 py-4 flex justify-end gap-3">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 rounded-lg transition-colors"
                    >
                        닫기
                    </button>
                    <button
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        상세 리포트 보기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EmployeeDetailModal;
