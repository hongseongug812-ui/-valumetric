import { useState } from 'react';
import api from '../../api';

interface Props {
    onSignUpSuccess: () => void;
    onBackToLogin: () => void;
}

export default function SignUpPage({ onSignUpSuccess, onBackToLogin }: Props) {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        companyName: '',
        currentGrade: '사원',
        currentSalary: '36000000'
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        // 비밀번호 확인
        if (formData.password !== formData.confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }

        if (formData.password.length < 4) {
            setError('비밀번호는 최소 4자 이상이어야 합니다.');
            return;
        }

        setLoading(true);

        try {
            await api.post('/auth/register', {
                name: formData.name,
                email: formData.email,
                password: formData.password,
                companyName: formData.companyName,
                currentGrade: formData.currentGrade,
                currentSalary: Number(formData.currentSalary)
            });

            alert('회원가입이 완료되었습니다! 로그인해주세요.');
            onSignUpSuccess();
        } catch (err: any) {
            console.error('회원가입 실패:', err);
            setError(err.response?.data?.message || '회원가입에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-900 via-indigo-900 to-blue-900 flex items-center justify-center p-4">
            <div className="w-full max-w-md">
                {/* 로고 */}
                <div className="text-center mb-6">
                    <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-gradient-to-r from-purple-500 to-blue-600 mb-3">
                        <span className="text-2xl">📊</span>
                    </div>
                    <h1 className="text-2xl font-bold text-white">ValuMetric</h1>
                    <p className="text-purple-200 mt-1 text-sm">인적자원 가치 측정 시스템</p>
                </div>

                {/* 회원가입 카드 */}
                <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-6 shadow-2xl border border-white/20">
                    <h2 className="text-lg font-semibold text-white mb-4 text-center">
                        ✨ 회원가입
                    </h2>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        {error && (
                            <div className="p-2 bg-red-500/20 border border-red-500/50 rounded-lg text-red-200 text-sm">
                                {error}
                            </div>
                        )}

                        {/* 회사명 */}
                        <div>
                            <label className="block text-sm font-medium text-purple-200 mb-1">
                                🏢 회사명
                            </label>
                            <input
                                type="text"
                                name="companyName"
                                value={formData.companyName}
                                onChange={handleChange}
                                className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                placeholder="예: 삼성전자, 네이버..."
                                required
                            />
                        </div>

                        {/* 이름 */}
                        <div>
                            <label className="block text-sm font-medium text-purple-200 mb-1">
                                👤 이름
                            </label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                placeholder="홍길동"
                                required
                            />
                        </div>

                        {/* 이메일 */}
                        <div>
                            <label className="block text-sm font-medium text-purple-200 mb-1">
                                ✉️ 이메일
                            </label>
                            <input
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                placeholder="example@company.com"
                                required
                            />
                        </div>

                        {/* 직급 & 연봉 */}
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label className="block text-sm font-medium text-purple-200 mb-1">
                                    📋 직급
                                </label>
                                <select
                                    name="currentGrade"
                                    value={formData.currentGrade}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                >
                                    <option value="사원" className="bg-gray-800">사원</option>
                                    <option value="대리" className="bg-gray-800">대리</option>
                                    <option value="과장" className="bg-gray-800">과장</option>
                                    <option value="차장" className="bg-gray-800">차장</option>
                                    <option value="부장" className="bg-gray-800">부장</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-purple-200 mb-1">
                                    💰 연봉 (만원)
                                </label>
                                <input
                                    type="number"
                                    name="currentSalary"
                                    value={Math.round(Number(formData.currentSalary) / 10000)}
                                    onChange={(e) => setFormData({
                                        ...formData,
                                        currentSalary: String(Number(e.target.value) * 10000)
                                    })}
                                    className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                    placeholder="3600"
                                />
                            </div>
                        </div>

                        {/* 비밀번호 */}
                        <div>
                            <label className="block text-sm font-medium text-purple-200 mb-1">
                                🔒 비밀번호
                            </label>
                            <input
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                placeholder="4자 이상"
                                required
                            />
                        </div>

                        {/* 비밀번호 확인 */}
                        <div>
                            <label className="block text-sm font-medium text-purple-200 mb-1">
                                🔒 비밀번호 확인
                            </label>
                            <input
                                type="password"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                className="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                placeholder="비밀번호 재입력"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-2.5 bg-gradient-to-r from-purple-500 to-blue-600 text-white font-semibold rounded-lg hover:from-purple-600 hover:to-blue-700 transition disabled:opacity-50"
                        >
                            {loading ? '가입 중...' : '회원가입'}
                        </button>
                    </form>

                    {/* 로그인으로 돌아가기 */}
                    <div className="mt-4 text-center">
                        <button
                            onClick={onBackToLogin}
                            className="text-purple-300 hover:text-white text-sm transition"
                        >
                            ← 로그인으로 돌아가기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
