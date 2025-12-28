import { useState, useEffect } from 'react';
import api from '../../api';

interface PerformanceLog {
    period: string;
    targetSales: number;
    achievedSales: number;
    profit: number;
}

interface ScoreHistory {
    criteriaName: string;
    newScore: number;
    reason: string;
}

interface EmployeeWithDetails {
    id: string;
    name: string;
    email: string;
    currentGrade: string;
    currentSalary: number;
    hireDate: string;
    role: string;
    isEnabled: boolean;
    performanceLogs: PerformanceLog[];
    scoreHistories: ScoreHistory[];
}

interface EmployeeForm {
    name: string;
    email: string;
    password: string;
    currentGrade: string;
    currentSalary: string;
    hireDate: string;
    role: string;
}

export default function EmployeeManagementPage() {
    const [employees, setEmployees] = useState<EmployeeWithDetails[]>([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [form, setForm] = useState<EmployeeForm>({
        name: '', email: '', password: '', currentGrade: '',
        currentSalary: '', hireDate: '', role: 'USER'
    });

    useEffect(() => {
        loadEmployees();
    }, []);

    const loadEmployees = async () => {
        try {
            // 전체 사원 상세 정보 가져오기
            const listRes = await api.get('/employees');
            const detailPromises = listRes.data.map((emp: any) =>
                api.get(`/employees/${emp.id}`)
            );
            const details = await Promise.all(detailPromises);
            setEmployees(details.map(d => d.data));
        } catch (error) {
            console.error('사원 목록 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const getLatestScore = (emp: EmployeeWithDetails) => {
        if (!emp.scoreHistories || emp.scoreHistories.length === 0) return null;
        return emp.scoreHistories[emp.scoreHistories.length - 1].newScore;
    };

    const getLatestPerformance = (emp: EmployeeWithDetails) => {
        if (!emp.performanceLogs || emp.performanceLogs.length === 0) return null;
        const sorted = [...emp.performanceLogs].sort((a, b) => b.period.localeCompare(a.period));
        return sorted[0];
    };

    const getAchievementRate = (log: PerformanceLog | null) => {
        if (!log || log.targetSales === 0) return null;
        return ((log.achievedSales / log.targetSales) * 100).toFixed(0);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const payload = { ...form, currentSalary: parseFloat(form.currentSalary) };
            if (editingId) {
                await api.put(`/employees/${editingId}`, payload);
            } else {
                await api.post('/employees', payload);
            }
            setShowForm(false);
            setEditingId(null);
            resetForm();
            loadEmployees();
        } catch (error: any) {
            alert(error.response?.data?.message || '저장 실패');
        }
    };

    const handleEdit = (emp: EmployeeWithDetails) => {
        setForm({
            name: emp.name, email: emp.email || '', password: '',
            currentGrade: emp.currentGrade || '',
            currentSalary: emp.currentSalary?.toString() || '',
            hireDate: emp.hireDate || '', role: emp.role
        });
        setEditingId(emp.id);
        setShowForm(true);
    };

    const handleDelete = async (id: string) => {
        if (!confirm('정말 삭제하시겠습니까?')) return;
        try {
            await api.delete(`/employees/${id}`);
            loadEmployees();
        } catch (error) {
            alert('삭제 실패');
        }
    };

    const generateSampleData = async () => {
        if (!confirm('테스트 데이터를 생성하시겠습니까?')) return;
        try {
            await api.post('/employees/sample-data');
            await api.post('/employees/init-scores');
            loadEmployees();
            alert('테스트 데이터 생성 완료!');
        } catch (error) {
            alert('생성 실패');
        }
    };

    const resetForm = () => {
        setForm({ name: '', email: '', password: '', currentGrade: '', currentSalary: '', hireDate: '', role: 'USER' });
    };

    if (loading) return <div className="p-8 text-center">로딩 중...</div>;

    return (
        <div className="p-6 max-w-7xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-800">👥 사원 관리</h1>
                <div className="space-x-2">
                    <button onClick={generateSampleData}
                        className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600">
                        📊 테스트 데이터 생성
                    </button>
                    <button onClick={() => { setShowForm(true); setEditingId(null); resetForm(); }}
                        className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600">
                        ➕ 신규 사원 등록
                    </button>
                </div>
            </div>

            {/* 등록/수정 폼 모달 */}
            {showForm && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md">
                        <h2 className="text-xl font-bold mb-4 text-gray-800">{editingId ? '사원 정보 수정' : '신규 사원 등록'}</h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <input type="text" placeholder="이름 *" value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 placeholder-gray-400 bg-white" required />
                            <input type="email" placeholder="이메일" value={form.email}
                                onChange={e => setForm({ ...form, email: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 placeholder-gray-400 bg-white" />
                            <input type="password" placeholder={editingId ? "비밀번호 (변경 시에만)" : "비밀번호 *"}
                                value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 placeholder-gray-400 bg-white" required={!editingId} />
                            <input type="text" placeholder="직급" value={form.currentGrade}
                                onChange={e => setForm({ ...form, currentGrade: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 placeholder-gray-400 bg-white" />
                            <input type="number" placeholder="연봉 *" value={form.currentSalary}
                                onChange={e => setForm({ ...form, currentSalary: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 placeholder-gray-400 bg-white" required />
                            <input type="date" value={form.hireDate}
                                onChange={e => setForm({ ...form, hireDate: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 bg-white" />
                            <select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}
                                className="w-full px-4 py-2 border rounded-lg text-gray-800 bg-white">
                                <option value="USER">일반 사원</option>
                                <option value="ADMIN">관리자</option>
                            </select>
                            <div className="flex gap-2">
                                <button type="button" onClick={() => { setShowForm(false); setEditingId(null); }}
                                    className="flex-1 px-4 py-2 bg-gray-300 text-gray-800 rounded-lg hover:bg-gray-400">취소</button>
                                <button type="submit"
                                    className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600">저장</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* 사원 카드 목록 */}
            {employees.length === 0 ? (
                <div className="bg-white rounded-xl shadow p-8 text-center text-gray-500">
                    등록된 사원이 없습니다. 테스트 데이터를 생성하세요.
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {employees.map(emp => {
                        const score = getLatestScore(emp);
                        const perf = getLatestPerformance(emp);
                        const rate = getAchievementRate(perf);

                        return (
                            <div key={emp.id} className="bg-white rounded-xl shadow p-5 hover:shadow-lg transition-shadow">
                                {/* 헤더: 이름, 직급, 역할 */}
                                <div className="flex justify-between items-start mb-4">
                                    <div>
                                        <h3 className="text-lg font-bold text-gray-800">{emp.name}</h3>
                                        <p className="text-sm text-gray-500">{emp.currentGrade || '직급 미지정'}</p>
                                    </div>
                                    <span className={`px-2 py-1 text-xs rounded ${emp.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-600'
                                        }`}>
                                        {emp.role === 'ADMIN' ? '관리자' : '사원'}
                                    </span>
                                </div>

                                {/* 점수 & 실적 */}
                                <div className="grid grid-cols-2 gap-3 mb-4">
                                    {/* 점수 */}
                                    <div className="bg-blue-50 rounded-lg p-3 text-center">
                                        <p className="text-xs text-gray-500 mb-1">현재 점수</p>
                                        <p className={`text-2xl font-bold ${score && score >= 900 ? 'text-green-600' :
                                            score && score >= 700 ? 'text-blue-600' :
                                                score ? 'text-red-600' : 'text-gray-400'
                                            }`}>
                                            {score ?? '-'}
                                        </p>
                                    </div>
                                    {/* 달성률 */}
                                    <div className="bg-green-50 rounded-lg p-3 text-center">
                                        <p className="text-xs text-gray-500 mb-1">달성률</p>
                                        <p className={`text-2xl font-bold ${rate && Number(rate) >= 100 ? 'text-green-600' :
                                            rate && Number(rate) >= 80 ? 'text-yellow-600' :
                                                rate ? 'text-red-600' : 'text-gray-400'
                                            }`}>
                                            {rate ? `${rate}%` : '-'}
                                        </p>
                                    </div>
                                </div>

                                {/* 최근 실적 */}
                                {perf && (
                                    <div className="bg-gray-50 rounded-lg p-3 mb-4 text-sm">
                                        <p className="text-xs text-gray-500 mb-2">최근 실적 ({perf.period})</p>
                                        <div className="flex justify-between">
                                            <span>목표: {(perf.targetSales / 10000).toFixed(0)}만원</span>
                                            <span>달성: {(perf.achievedSales / 10000).toFixed(0)}만원</span>
                                        </div>
                                    </div>
                                )}

                                {/* 추가 정보 */}
                                <div className="text-sm text-gray-500 mb-4">
                                    <p>연봉: {emp.currentSalary?.toLocaleString()}원</p>
                                    <p>실적: {emp.performanceLogs?.length || 0}건 | 입사일: {emp.hireDate || '-'}</p>
                                </div>

                                {/* 액션 버튼 */}
                                <div className="flex gap-2">
                                    <button onClick={() => handleEdit(emp)}
                                        className="flex-1 px-3 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 text-sm">
                                        ✏️ 수정
                                    </button>
                                    <button onClick={() => handleDelete(emp.id)}
                                        className="flex-1 px-3 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 text-sm">
                                        🗑️ 삭제
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
