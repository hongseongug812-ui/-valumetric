import { useState, useEffect } from 'react';
import api from '../../api';

interface Employee {
    id: string;
    name: string;
    currentGrade: string;
}

interface PerformanceForm {
    employeeId: string;
    period: string;
    targetSales: string;
    achievedSales: string;
    profit: string;
}

export default function PerformanceInputPage() {
    const [employees, setEmployees] = useState<Employee[]>([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [form, setForm] = useState<PerformanceForm>({
        employeeId: '',
        period: new Date().toISOString().slice(0, 7), // YYYY-MM
        targetSales: '',
        achievedSales: '',
        profit: ''
    });

    useEffect(() => {
        loadEmployees();
    }, []);

    const loadEmployees = async () => {
        try {
            const res = await api.get('/employees');
            setEmployees(res.data);
        } catch (error) {
            console.error('사원 목록 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.employeeId) {
            alert('사원을 선택해주세요');
            return;
        }

        setSubmitting(true);
        try {
            await api.post('/employees/performance', {
                employeeId: form.employeeId,
                period: form.period,
                targetSales: parseFloat(form.targetSales),
                achievedSales: parseFloat(form.achievedSales),
                profit: parseFloat(form.profit)
            });

            alert('실적이 저장되었습니다!');
            setForm({
                ...form,
                targetSales: '',
                achievedSales: '',
                profit: ''
            });
        } catch (error: any) {
            alert(error.response?.data?.message || '저장 실패');
        } finally {
            setSubmitting(false);
        }
    };

    // 달성률 자동 계산
    const achievementRate = form.targetSales && form.achievedSales
        ? ((parseFloat(form.achievedSales) / parseFloat(form.targetSales)) * 100).toFixed(1)
        : null;

    if (loading) return <div className="p-8 text-center">로딩 중...</div>;

    return (
        <div className="p-6 max-w-2xl mx-auto">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">📈 월별 실적 입력</h1>

            <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow p-6 space-y-6">
                {/* 사원 선택 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        사원 선택 *
                    </label>
                    <select
                        value={form.employeeId}
                        onChange={e => setForm({ ...form, employeeId: e.target.value })}
                        className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                        required
                    >
                        <option value="">-- 사원 선택 --</option>
                        {employees.map(emp => (
                            <option key={emp.id} value={emp.id}>
                                {emp.name} ({emp.currentGrade || '직급 미지정'})
                            </option>
                        ))}
                    </select>
                </div>

                {/* 기간 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        기간 *
                    </label>
                    <input
                        type="month"
                        value={form.period}
                        onChange={e => setForm({ ...form, period: e.target.value })}
                        className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                        required
                    />
                </div>

                {/* 목표 매출 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        목표 매출 (원) *
                    </label>
                    <input
                        type="number"
                        value={form.targetSales}
                        onChange={e => setForm({ ...form, targetSales: e.target.value })}
                        placeholder="예: 10000000"
                        className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                        required
                    />
                </div>

                {/* 달성 매출 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        달성 매출 (원) *
                    </label>
                    <input
                        type="number"
                        value={form.achievedSales}
                        onChange={e => setForm({ ...form, achievedSales: e.target.value })}
                        placeholder="예: 12000000"
                        className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                        required
                    />
                    {achievementRate && (
                        <p className={`mt-2 text-sm ${parseFloat(achievementRate) >= 100
                                ? 'text-green-600'
                                : 'text-red-600'
                            }`}>
                            달성률: {achievementRate}%
                        </p>
                    )}
                </div>

                {/* 이익 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        이익 (원)
                    </label>
                    <input
                        type="number"
                        value={form.profit}
                        onChange={e => setForm({ ...form, profit: e.target.value })}
                        placeholder="예: 1500000"
                        className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                {/* 제출 버튼 */}
                <button
                    type="submit"
                    disabled={submitting}
                    className={`w-full py-3 rounded-lg text-white font-medium ${submitting
                            ? 'bg-gray-400 cursor-not-allowed'
                            : 'bg-blue-500 hover:bg-blue-600'
                        }`}
                >
                    {submitting ? '저장 중...' : '💾 실적 저장'}
                </button>
            </form>

            {/* 안내 */}
            <div className="mt-6 bg-blue-50 rounded-lg p-4 text-sm text-blue-800">
                <p className="font-medium mb-2">💡 안내</p>
                <ul className="list-disc list-inside space-y-1">
                    <li>동일 기간에 실적을 다시 입력하면 기존 데이터가 덮어씌워집니다.</li>
                    <li>달성률이 100% 미만이면 HCROI가 낮게 계산됩니다.</li>
                    <li>실적은 대시보드에 반영됩니다.</li>
                </ul>
            </div>
        </div>
    );
}
