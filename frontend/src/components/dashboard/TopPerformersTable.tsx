interface TopPerformer {
    employeeId: string;
    employeeName: string;
    currentGrade: string;
    currentSalary: number;
    currentHcroi: number;
    currentScore: number;
    targetAchievementRate: number;
    performanceLevel: 'EXCELLENT' | 'OUTSTANDING';
    achievement: string;
    consecutiveMonths: number;
}

interface Props {
    performers: TopPerformer[];
    onEmployeeClick?: (employeeId: string) => void;
}

export default function TopPerformersTable({ performers, onEmployeeClick }: Props) {
    if (performers.length === 0) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    🏆 우수 사원
                </h2>
                <p className="text-gray-500 text-center py-8">
                    우수 사원이 없습니다. (HCROI ≥ 1.5 또는 점수 ≥ 900)
                </p>
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white">
                    🏆 우수 사원 ({performers.length}명)
                </h2>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead className="bg-gradient-to-r from-yellow-50 to-green-50 dark:from-yellow-900/20 dark:to-green-900/20">
                        <tr>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-600 dark:text-gray-300">사원</th>
                            <th className="px-4 py-3 text-center text-sm font-medium text-gray-600 dark:text-gray-300">등급</th>
                            <th className="px-4 py-3 text-right text-sm font-medium text-gray-600 dark:text-gray-300">HCROI</th>
                            <th className="px-4 py-3 text-right text-sm font-medium text-gray-600 dark:text-gray-300">점수</th>
                            <th className="px-4 py-3 text-center text-sm font-medium text-gray-600 dark:text-gray-300">연속달성</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-600 dark:text-gray-300">성과</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                        {performers.map((emp) => (
                            <tr
                                key={emp.employeeId}
                                onClick={() => onEmployeeClick?.(emp.employeeId)}
                                className="hover:bg-green-50 dark:hover:bg-green-900/10 cursor-pointer transition-colors"
                            >
                                <td className="px-4 py-3">
                                    <div className="flex items-center gap-2">
                                        <span className="text-lg">
                                            {emp.performanceLevel === 'OUTSTANDING' ? '🌟' : '⭐'}
                                        </span>
                                        <div>
                                            <p className="font-medium text-gray-800 dark:text-white">
                                                {emp.employeeName}
                                            </p>
                                            <p className="text-xs text-gray-500">{emp.currentGrade}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-4 py-3 text-center">
                                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${emp.performanceLevel === 'OUTSTANDING'
                                            ? 'bg-gradient-to-r from-yellow-400 to-yellow-500 text-white'
                                            : 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                                        }`}>
                                        {emp.performanceLevel === 'OUTSTANDING' ? '최우수' : '우수'}
                                    </span>
                                </td>
                                <td className="px-4 py-3 text-right">
                                    <span className="font-bold text-green-600 dark:text-green-400">
                                        {emp.currentHcroi?.toFixed(2)}
                                    </span>
                                </td>
                                <td className="px-4 py-3 text-right">
                                    <span className="font-medium text-gray-800 dark:text-white">
                                        {emp.currentScore?.toFixed(0)}점
                                    </span>
                                </td>
                                <td className="px-4 py-3 text-center">
                                    {emp.consecutiveMonths > 0 && (
                                        <span className="text-orange-500 font-medium">
                                            🔥 {emp.consecutiveMonths}개월
                                        </span>
                                    )}
                                </td>
                                <td className="px-4 py-3">
                                    <span className="text-sm text-gray-600 dark:text-gray-400">
                                        {emp.achievement}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
