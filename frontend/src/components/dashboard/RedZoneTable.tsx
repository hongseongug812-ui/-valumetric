import type { RedZoneEmployee } from '../../types';

interface RedZoneTableProps {
    employees: RedZoneEmployee[];
    onEmployeeClick?: (employeeId: number) => void;
}

const RedZoneTable = ({ employees, onEmployeeClick }: RedZoneTableProps) => {
    if (employees.length === 0) {
        return (
            <div className="card text-center py-12">
                <span className="text-6xl mb-4 block">✅</span>
                <p className="text-gray-500">위험군 사원이 없습니다</p>
            </div>
        );
    }

    return (
        <div className="card overflow-hidden">
            <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                <span>⚠️</span>
                <span>위험군(Red Zone) 사원</span>
                <span className="bg-red-100 text-red-800 text-sm px-2 py-1 rounded-full">
                    {employees.length}명
                </span>
            </h3>

            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead>
                        <tr className="border-b border-gray-200 dark:border-gray-700">
                            <th className="text-left py-3 px-4 font-medium text-gray-500">사원명</th>
                            <th className="text-left py-3 px-4 font-medium text-gray-500">등급</th>
                            <th className="text-right py-3 px-4 font-medium text-gray-500">HCROI</th>
                            <th className="text-right py-3 px-4 font-medium text-gray-500">점수</th>
                            <th className="text-center py-3 px-4 font-medium text-gray-500">위험도</th>
                            <th className="text-left py-3 px-4 font-medium text-gray-500">사유</th>
                        </tr>
                    </thead>
                    <tbody>
                        {employees.map((emp) => (
                            <tr
                                key={emp.employeeId}
                                onClick={() => onEmployeeClick?.(emp.employeeId)}
                                className="border-b border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
                            >
                                <td className="py-4 px-4 font-medium">{emp.employeeName}</td>
                                <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{emp.currentGrade}</td>
                                <td className={`py-4 px-4 text-right font-mono ${emp.currentHcroi < 1 ? 'text-red-600' : 'text-green-600'
                                    }`}>
                                    {emp.currentHcroi?.toFixed(4)}
                                </td>
                                <td className={`py-4 px-4 text-right font-mono ${emp.currentScore < 700 ? 'text-red-600' : 'text-green-600'
                                    }`}>
                                    {emp.currentScore?.toFixed(0)}
                                </td>
                                <td className="py-4 px-4 text-center">
                                    <span className={emp.riskLevel === 'CRITICAL' ? 'badge-critical' : 'badge-warning'}>
                                        {emp.riskLevel}
                                    </span>
                                </td>
                                <td className="py-4 px-4 text-sm text-gray-500">{emp.riskReason}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default RedZoneTable;
