import { useState, useEffect } from 'react';
import api from '../../api';

interface UpcomingBirthday {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    birthDate: string;
    daysUntilBirthday: number;
    message: string;
}

export default function UpcomingBirthdaysSidebar() {
    const [birthdays, setBirthdays] = useState<UpcomingBirthday[]>([]);
    const [loading, setLoading] = useState(true);
    const [isCollapsed, setIsCollapsed] = useState(false);

    useEffect(() => {
        const fetchBirthdays = async () => {
            try {
                const res = await api.get('/dashboard/birthdays');
                setBirthdays(res.data);
            } catch (error) {
                console.error('생일 데이터 로드 실패:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchBirthdays();
    }, []);

    if (loading) {
        return (
            <div className="fixed right-0 top-20 w-72 bg-white dark:bg-gray-800 shadow-lg rounded-l-xl p-4 border-l border-gray-200 dark:border-gray-700">
                <div className="animate-pulse space-y-3">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-10 bg-gray-200 rounded"></div>
                    <div className="h-10 bg-gray-200 rounded"></div>
                </div>
            </div>
        );
    }

    if (birthdays.length === 0 && !isCollapsed) {
        return (
            <div className="fixed right-0 top-20 w-72 bg-white dark:bg-gray-800 shadow-lg rounded-l-xl p-4 border-l border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-3">
                    <h3 className="font-semibold text-gray-800 dark:text-gray-100 flex items-center gap-2">
                        🎂 다가오는 생일
                    </h3>
                </div>
                <p className="text-gray-500 text-sm text-center py-4">
                    30일 내 생일자가 없습니다.
                </p>
            </div>
        );
    }

    return (
        <>
            {/* 축소 버튼 */}
            {isCollapsed && (
                <button
                    onClick={() => setIsCollapsed(false)}
                    className="fixed right-0 top-20 bg-gradient-to-r from-pink-500 to-purple-500 text-white p-3 rounded-l-xl shadow-lg hover:from-pink-600 hover:to-purple-600 transition"
                    title="생일 목록 열기"
                >
                    🎂
                    {birthdays.length > 0 && (
                        <span className="absolute -top-1 -left-1 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center">
                            {birthdays.length}
                        </span>
                    )}
                </button>
            )}

            {/* 사이드바 */}
            {!isCollapsed && (
                <div className="fixed right-0 top-20 w-72 bg-white dark:bg-gray-800 shadow-lg rounded-l-xl border-l border-gray-200 dark:border-gray-700 overflow-hidden">
                    {/* 헤더 */}
                    <div className="bg-gradient-to-r from-pink-500 to-purple-500 text-white p-3 flex items-center justify-between">
                        <h3 className="font-semibold flex items-center gap-2">
                            🎂 다가오는 생일
                            <span className="bg-white/20 px-2 py-0.5 rounded text-sm">
                                {birthdays.length}명
                            </span>
                        </h3>
                        <button
                            onClick={() => setIsCollapsed(true)}
                            className="hover:bg-white/20 p-1 rounded transition"
                            title="접기"
                        >
                            ✕
                        </button>
                    </div>

                    {/* 생일 목록 */}
                    <div className="max-h-80 overflow-y-auto">
                        {birthdays.map((birthday, index) => (
                            <div
                                key={birthday.employeeId}
                                className={`p-3 flex items-center gap-3 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition ${birthday.daysUntilBirthday === 0 ? 'bg-pink-50 dark:bg-pink-900/20' : ''
                                    }`}
                            >
                                {/* 순서 */}
                                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${birthday.daysUntilBirthday === 0
                                        ? 'bg-pink-500 text-white'
                                        : birthday.daysUntilBirthday <= 3
                                            ? 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-300'
                                            : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300'
                                    }`}>
                                    {index + 1}
                                </div>

                                {/* 정보 */}
                                <div className="flex-1 min-w-0">
                                    <div className="font-medium text-gray-800 dark:text-gray-100 truncate">
                                        {birthday.employeeName}
                                    </div>
                                    <div className="text-xs text-gray-500 dark:text-gray-400">
                                        {birthday.currentGrade} · {new Date(birthday.birthDate).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' })}
                                    </div>
                                </div>

                                {/* D-day */}
                                <div className={`px-2 py-1 rounded text-xs font-medium ${birthday.daysUntilBirthday === 0
                                        ? 'bg-pink-500 text-white'
                                        : birthday.daysUntilBirthday <= 3
                                            ? 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-300'
                                            : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300'
                                    }`}>
                                    {birthday.message}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </>
    );
}
