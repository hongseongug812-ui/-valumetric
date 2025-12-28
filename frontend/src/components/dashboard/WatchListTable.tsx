import type { WatchListEmployee } from '../../types';

interface Props {
    watchList: WatchListEmployee[];
    onEmployeeClick?: (employeeId: string) => void;
}

export default function WatchListTable({ watchList, onEmployeeClick }: Props) {
    if (watchList.length === 0) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-4">
                    👀 잠재적 관리 대상 (Watch List)
                </h2>
                <div className="text-center py-8">
                    <div className="text-4xl mb-2">✅</div>
                    <p className="text-gray-500 dark:text-gray-400">
                        모든 사원이 안정적입니다.
                    </p>
                    <p className="text-sm text-gray-400 mt-2">
                        Yellow Zone 사원이 없습니다. (HCROI ≥ 1.2 & 점수 ≥ 750)
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow overflow-hidden">
            <div className="p-4 border-b border-gray-200 dark:border-gray-700">
                <div className="flex justify-between items-center">
                    <h2 className="text-lg font-bold text-gray-800 dark:text-white">
                        👀 잠재적 관리 대상 (Watch List)
                    </h2>
                    <span className="text-sm text-yellow-600 dark:text-yellow-400">
                        {watchList.length}명 주의 필요
                    </span>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                    Red Zone은 아니지만 주의가 필요한 사원입니다.
                </p>
            </div>

            <table className="w-full">
                <thead className="bg-yellow-50 dark:bg-yellow-900/20">
                    <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-600 dark:text-gray-400">사원</th>
                        <th className="px-4 py-2 text-center text-xs font-medium text-gray-600 dark:text-gray-400">HCROI</th>
                        <th className="px-4 py-2 text-center text-xs font-medium text-gray-600 dark:text-gray-400">점수</th>
                        <th className="px-4 py-2 text-center text-xs font-medium text-gray-600 dark:text-gray-400">변화</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-600 dark:text-gray-400">사유</th>
                    </tr>
                </thead>
                <tbody>
                    {watchList.map((emp) => (
                        <tr
                            key={emp.employeeId}
                            className="border-t border-gray-100 dark:border-gray-700 hover:bg-yellow-50 dark:hover:bg-yellow-900/10 cursor-pointer transition"
                            onClick={() => onEmployeeClick?.(emp.employeeId)}
                        >
                            <td className="px-4 py-3">
                                <div className="flex items-center gap-2">
                                    <div className={`w-2 h-2 rounded-full ${emp.riskLevel === 'ORANGE' ? 'bg-orange-500' : 'bg-yellow-400'
                                        }`} />
                                    <div>
                                        <div className="font-medium text-gray-900 dark:text-white">
                                            {emp.employeeName}
                                        </div>
                                        <div className="text-xs text-gray-500">
                                            {emp.currentGrade}
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td className="px-4 py-3 text-center">
                                <span className={`font-bold ${emp.currentHcroi >= 1.2 ? 'text-green-600' : 'text-yellow-600'
                                    }`}>
                                    {emp.currentHcroi?.toFixed(2)}
                                </span>
                            </td>
                            <td className="px-4 py-3 text-center">
                                <span className={`font-bold ${emp.currentScore >= 750 ? 'text-green-600' : 'text-yellow-600'
                                    }`}>
                                    {emp.currentScore}
                                </span>
                            </td>
                            <td className="px-4 py-3 text-center">
                                {emp.hcroiChange != null && (
                                    <span className={`text-sm font-medium ${emp.hcroiChange >= 0 ? 'text-green-600' : 'text-red-600'
                                        }`}>
                                        {emp.hcroiChange >= 0 ? '↑' : '↓'}
                                        {Math.abs(emp.hcroiChange).toFixed(2)}
                                    </span>
                                )}
                            </td>
                            <td className="px-4 py-3">
                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${emp.riskLevel === 'ORANGE'
                                    ? 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400'
                                    : 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400'
                                    }`}>
                                    {emp.watchReason}
                                </span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* 범례 */}
            <div className="px-4 py-3 bg-gray-50 dark:bg-gray-900/50 border-t border-gray-200 dark:border-gray-700">
                <div className="flex gap-4 text-xs text-gray-500">
                    <span className="flex items-center gap-1">
                        <span className="w-2 h-2 rounded-full bg-yellow-400"></span>
                        Yellow Zone: 커트라인 근접
                    </span>
                    <span className="flex items-center gap-1">
                        <span className="w-2 h-2 rounded-full bg-orange-500"></span>
                        Orange Zone: 근접 + 하락세
                    </span>
                </div>
            </div>
        </div>
    );
}
