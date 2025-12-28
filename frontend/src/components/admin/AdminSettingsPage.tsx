import { useState, useEffect, useCallback } from 'react';
import { adminApi } from '../../api';
import type { AhpWeightResponse, SalaryConfig } from '../../types';

interface AdminSettingsPageProps {
    onSave?: () => void;
}

const AdminSettingsPage = ({ onSave }: AdminSettingsPageProps) => {
    // 가중치 상태 (합계 = 1)
    const [salesWeight, setSalesWeight] = useState(50);      // 매출 (%)
    const [attendanceWeight, setAttendanceWeight] = useState(30); // 근태 (%)
    const [otherWeight, setOtherWeight] = useState(20);      // 기타 (%)

    // 급여 설정 상태
    const [salaryConfig, setSalaryConfig] = useState<SalaryConfig>({
        id: 1,
        fixedCostPerPerson: 500000,
        insuranceRate: 0.0945,
        targetProfitRate: 0.15
    });

    const [loading, setLoading] = useState(false);
    const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved' | 'error'>('idle');
    const [ahpResult, setAhpResult] = useState<AhpWeightResponse | null>(null);

    // 초기 데이터 로드
    useEffect(() => {
        const fetchSettings = async () => {
            try {
                const [weightsRes, configRes] = await Promise.all([
                    adminApi.getAhpWeights(),
                    adminApi.getSalaryConfig()
                ]);

                if (weightsRes.data.weights.length >= 3) {
                    setSalesWeight(weightsRes.data.weights[0] * 100);
                    setAttendanceWeight(weightsRes.data.weights[1] * 100);
                    setOtherWeight(weightsRes.data.weights[2] * 100);
                }
                setSalaryConfig(configRes.data);
                setAhpResult(weightsRes.data);
            } catch (error) {
                console.error('설정 로드 실패:', error);
            }
        };

        fetchSettings();
    }, []);

    // 매출 가중치 변경 핸들러 (다른 값 자동 조정)
    const handleSalesChange = useCallback((value: number) => {
        const remaining = 100 - value;
        const ratio = attendanceWeight / (attendanceWeight + otherWeight) || 0.6;

        setSalesWeight(value);
        setAttendanceWeight(Math.round(remaining * ratio));
        setOtherWeight(Math.round(remaining * (1 - ratio)));
    }, [attendanceWeight, otherWeight]);

    // 근태 가중치 변경 핸들러
    const handleAttendanceChange = useCallback((value: number) => {
        const maxValue = 100 - salesWeight;
        const adjustedValue = Math.min(value, maxValue);

        setAttendanceWeight(adjustedValue);
        setOtherWeight(100 - salesWeight - adjustedValue);
    }, [salesWeight]);

    // 가중치 저장
    const handleSaveWeights = async () => {
        setLoading(true);
        setSaveStatus('saving');

        try {
            const response = await adminApi.setAhpWeights({
                criteriaNames: ['매출성과', '근태', '기타성과'],
                weights: [salesWeight / 100, attendanceWeight / 100, otherWeight / 100]
            });

            setAhpResult(response.data);
            setSaveStatus('saved');
            onSave?.();

            setTimeout(() => setSaveStatus('idle'), 2000);
        } catch (error) {
            console.error('가중치 저장 실패:', error);
            setSaveStatus('error');
        } finally {
            setLoading(false);
        }
    };

    // 급여 설정 저장
    const handleSaveSalaryConfig = async () => {
        setLoading(true);
        setSaveStatus('saving');

        try {
            await adminApi.updateSalaryConfig(salaryConfig);
            setSaveStatus('saved');
            onSave?.();

            setTimeout(() => setSaveStatus('idle'), 2000);
        } catch (error) {
            console.error('급여 설정 저장 실패:', error);
            setSaveStatus('error');
        } finally {
            setLoading(false);
        }
    };

    // 금액 포맷
    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('ko-KR').format(value);

    return (
        <div className="space-y-8">
            {/* AHP 가중치 설정 */}
            <div className="card">
                <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
                    <span>⚖️</span>
                    평가 기준 가중치 설정
                </h2>

                <p className="text-gray-500 mb-6">
                    슬라이더를 움직여 각 평가 기준의 중요도를 설정하세요. 합계는 자동으로 100%가 됩니다.
                </p>

                {/* 가중치 시각화 바 */}
                <div className="mb-8">
                    <div className="flex h-12 rounded-xl overflow-hidden shadow-inner">
                        <div
                            className="bg-gradient-to-r from-blue-500 to-blue-600 flex items-center justify-center text-white font-bold text-sm transition-all duration-300"
                            style={{ width: `${salesWeight}%` }}
                        >
                            {salesWeight >= 15 && `매출 ${salesWeight}%`}
                        </div>
                        <div
                            className="bg-gradient-to-r from-green-500 to-green-600 flex items-center justify-center text-white font-bold text-sm transition-all duration-300"
                            style={{ width: `${attendanceWeight}%` }}
                        >
                            {attendanceWeight >= 15 && `근태 ${attendanceWeight}%`}
                        </div>
                        <div
                            className="bg-gradient-to-r from-purple-500 to-purple-600 flex items-center justify-center text-white font-bold text-sm transition-all duration-300"
                            style={{ width: `${otherWeight}%` }}
                        >
                            {otherWeight >= 15 && `기타 ${otherWeight}%`}
                        </div>
                    </div>
                </div>

                {/* 매출 vs 근태 비교 슬라이더 */}
                <div className="space-y-6">
                    {/* 매출 성과 */}
                    <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-5">
                        <div className="flex items-center justify-between mb-3">
                            <label className="flex items-center gap-2 font-medium">
                                <span className="text-2xl">💰</span>
                                <span>매출 성과</span>
                            </label>
                            <span className="text-2xl font-bold text-blue-600">{salesWeight}%</span>
                        </div>
                        <input
                            type="range"
                            min="10"
                            max="80"
                            value={salesWeight}
                            onChange={(e) => handleSalesChange(Number(e.target.value))}
                            className="w-full h-3 bg-blue-200 rounded-lg appearance-none cursor-pointer slider-blue"
                        />
                        <div className="flex justify-between text-xs text-gray-400 mt-1">
                            <span>10%</span>
                            <span>80%</span>
                        </div>
                    </div>

                    {/* 근태 */}
                    <div className="bg-green-50 dark:bg-green-900/20 rounded-xl p-5">
                        <div className="flex items-center justify-between mb-3">
                            <label className="flex items-center gap-2 font-medium">
                                <span className="text-2xl">📅</span>
                                <span>근태</span>
                            </label>
                            <span className="text-2xl font-bold text-green-600">{attendanceWeight}%</span>
                        </div>
                        <input
                            type="range"
                            min="5"
                            max={100 - salesWeight - 5}
                            value={attendanceWeight}
                            onChange={(e) => handleAttendanceChange(Number(e.target.value))}
                            className="w-full h-3 bg-green-200 rounded-lg appearance-none cursor-pointer slider-green"
                        />
                        <div className="flex justify-between text-xs text-gray-400 mt-1">
                            <span>5%</span>
                            <span>{100 - salesWeight - 5}%</span>
                        </div>
                    </div>

                    {/* 기타 성과 (자동 계산) */}
                    <div className="bg-purple-50 dark:bg-purple-900/20 rounded-xl p-5">
                        <div className="flex items-center justify-between">
                            <label className="flex items-center gap-2 font-medium">
                                <span className="text-2xl">📊</span>
                                <span>기타 성과</span>
                                <span className="text-xs text-gray-400 ml-2">(자동 계산)</span>
                            </label>
                            <span className="text-2xl font-bold text-purple-600">{otherWeight}%</span>
                        </div>
                    </div>
                </div>

                {/* 저장 버튼 */}
                <div className="mt-6 flex items-center justify-between">
                    <div className="text-sm text-gray-500">
                        {ahpResult?.isConsistent !== undefined && (
                            <span className={ahpResult.isConsistent ? 'text-green-600' : 'text-red-600'}>
                                {ahpResult.isConsistent ? '✓ 일관성 충족' : '⚠️ 일관성 검토 필요'}
                            </span>
                        )}
                    </div>
                    <button
                        onClick={handleSaveWeights}
                        disabled={loading}
                        className="px-6 py-2.5 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
                    >
                        {saveStatus === 'saving' && <span className="animate-spin">⏳</span>}
                        {saveStatus === 'saved' && <span>✓</span>}
                        {saveStatus === 'error' && <span>✗</span>}
                        가중치 저장
                    </button>
                </div>
            </div>

            {/* 급여 설정 */}
            <div className="card">
                <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
                    <span>💵</span>
                    급여 및 비용 설정
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* 1인당 고정비 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            1인당 고정비 (월)
                        </label>
                        <div className="relative">
                            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">₩</span>
                            <input
                                type="number"
                                value={salaryConfig.fixedCostPerPerson}
                                onChange={(e) => setSalaryConfig({
                                    ...salaryConfig,
                                    fixedCostPerPerson: Number(e.target.value)
                                })}
                                className="w-full pl-8 pr-4 py-3 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>
                        <p className="text-xs text-gray-400 mt-1">
                            현재: {formatCurrency(salaryConfig.fixedCostPerPerson)}원
                        </p>
                    </div>

                    {/* 4대보험료율 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            4대보험료율
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                step="0.0001"
                                value={salaryConfig.insuranceRate}
                                onChange={(e) => setSalaryConfig({
                                    ...salaryConfig,
                                    insuranceRate: Number(e.target.value)
                                })}
                                className="w-full px-4 py-3 pr-8 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                            <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">%</span>
                        </div>
                        <p className="text-xs text-gray-400 mt-1">
                            현재: {(salaryConfig.insuranceRate * 100).toFixed(2)}%
                        </p>
                    </div>

                    {/* 목표 이익률 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            목표 이익률
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                step="0.01"
                                value={salaryConfig.targetProfitRate}
                                onChange={(e) => setSalaryConfig({
                                    ...salaryConfig,
                                    targetProfitRate: Number(e.target.value)
                                })}
                                className="w-full px-4 py-3 pr-8 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                            <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">%</span>
                        </div>
                        <p className="text-xs text-gray-400 mt-1">
                            현재: {(salaryConfig.targetProfitRate * 100).toFixed(0)}%
                        </p>
                    </div>
                </div>

                {/* 저장 버튼 */}
                <div className="mt-6 flex justify-end">
                    <button
                        onClick={handleSaveSalaryConfig}
                        disabled={loading}
                        className="px-6 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        급여 설정 저장
                    </button>
                </div>
            </div>

            {/* 슬라이더 커스텀 스타일 */}
            <style>{`
        .slider-blue::-webkit-slider-thumb {
          appearance: none;
          width: 24px;
          height: 24px;
          background: linear-gradient(135deg, #3b82f6, #2563eb);
          border-radius: 50%;
          cursor: pointer;
          box-shadow: 0 2px 6px rgba(59, 130, 246, 0.4);
        }
        .slider-green::-webkit-slider-thumb {
          appearance: none;
          width: 24px;
          height: 24px;
          background: linear-gradient(135deg, #10b981, #059669);
          border-radius: 50%;
          cursor: pointer;
          box-shadow: 0 2px 6px rgba(16, 185, 129, 0.4);
        }
      `}</style>
        </div>
    );
};

export default AdminSettingsPage;
