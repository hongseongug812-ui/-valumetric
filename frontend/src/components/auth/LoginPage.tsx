import { useState } from 'react';
import api from '../../api';

interface Props {
    onLoginSuccess: (user: { id: string; name: string; role: string; companyName?: string }) => void;
    onSignUp?: () => void;
}

export default function LoginPage({ onLoginSuccess, onSignUp }: Props) {
    const [employeeId, setEmployeeId] = useState('');
    const [password, setPassword] = useState('');
    const [companyName, setCompanyName] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const res = await api.post('/auth/login', {
                employeeId,
                password
            });

            const { accessToken, refreshToken, employeeId: id, employeeName, role } = res.data;

            // 토큰 및 회사명 저장
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('companyName', companyName || 'ValuMetric');
            localStorage.setItem('user', JSON.stringify({ id, name: employeeName, role, companyName: companyName || 'ValuMetric' }));

            onLoginSuccess({ id, name: employeeName, role, companyName: companyName || 'ValuMetric' });
        } catch (err: any) {
            console.error('로그인 실패:', err);
            setError(err.response?.data?.message || '로그인에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 데모용 빠른 로그인
    const handleDemoLogin = () => {
        if (!companyName.trim()) {
            setError('회사명을 입력해주세요.');
            return;
        }
        localStorage.setItem('companyName', companyName);
        localStorage.setItem('user', JSON.stringify({ id: 'demo', name: '관리자', role: 'ADMIN', companyName }));
        onLoginSuccess({ id: 'demo', name: '관리자', role: 'ADMIN', companyName });
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-900 via-indigo-900 to-purple-900 flex items-center justify-center p-4">
            <div className="w-full max-w-md">
                {/* 로고 */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 mb-4">
                        <span className="text-3xl">📊</span>
                    </div>
                    <h1 className="text-3xl font-bold text-white">ValuMetric</h1>
                    <p className="text-blue-200 mt-2">인적자원 가치 측정 시스템</p>
                </div>

                {/* 로그인 카드 */}
                <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 shadow-2xl border border-white/20">
                    <h2 className="text-xl font-semibold text-white mb-6 text-center">
                        로그인
                    </h2>

                    <form onSubmit={handleSubmit} className="space-y-5">
                        {error && (
                            <div className="p-3 bg-red-500/20 border border-red-500/50 rounded-lg text-red-200 text-sm">
                                {error}
                            </div>
                        )}

                        {/* 회사명 입력 */}
                        <div>
                            <label className="block text-sm font-medium text-blue-200 mb-2">
                                🏢 회사명
                            </label>
                            <input
                                type="text"
                                value={companyName}
                                onChange={(e) => setCompanyName(e.target.value)}
                                className="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                                placeholder="예: 삼성전자, 네이버, 카카오..."
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-blue-200 mb-2">
                                사원 ID 또는 이메일
                            </label>
                            <input
                                type="text"
                                value={employeeId}
                                onChange={(e) => setEmployeeId(e.target.value)}
                                className="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                                placeholder="employee@valumetric.com"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-blue-200 mb-2">
                                비밀번호
                            </label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                                placeholder="••••••••"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-purple-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <span className="flex items-center justify-center gap-2">
                                    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                    </svg>
                                    로그인 중...
                                </span>
                            ) : '로그인'}
                        </button>
                    </form>

                    {/* 구분선 */}
                    <div className="flex items-center my-6">
                        <div className="flex-1 border-t border-white/20"></div>
                        <span className="px-4 text-sm text-blue-200">또는</span>
                        <div className="flex-1 border-t border-white/20"></div>
                    </div>

                    {/* 데모 로그인 */}
                    <button
                        onClick={handleDemoLogin}
                        className="w-full py-3 bg-white/10 border border-white/30 text-white font-medium rounded-lg hover:bg-white/20 transition"
                    >
                        🚀 데모로 바로 입장
                    </button>

                    <p className="text-center text-xs text-blue-300 mt-4">
                        데모 모드는 회사명 입력 후 인증 없이 대시보드를 체험할 수 있습니다.
                    </p>

                    {/* 회원가입 링크 */}
                    {onSignUp && (
                        <div className="mt-4 pt-4 border-t border-white/20 text-center">
                            <span className="text-blue-200 text-sm">계정이 없으신가요? </span>
                            <button
                                onClick={onSignUp}
                                className="text-white font-medium text-sm hover:underline"
                            >
                                회원가입 →
                            </button>
                        </div>
                    )}
                </div>

                {/* 푸터 */}
                <p className="text-center text-blue-300/60 text-sm mt-8">
                    © 2024 ValuMetric. HCROI 기반 인적자원 가치 측정 시스템
                </p>
            </div>
        </div>
    );
}

