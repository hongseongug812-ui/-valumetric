import type { DashboardSummary } from '../../types';

interface SummaryCardsProps {
    summary: DashboardSummary | null;
    loading?: boolean;
}

const SummaryCards = ({ summary, loading }: SummaryCardsProps) => {
    if (loading) {
        return (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[...Array(4)].map((_, i) => (
                    <div key={i} className="card animate-pulse">
                        <div className="h-4 bg-gray-200 rounded w-1/2 mb-4"></div>
                        <div className="h-8 bg-gray-200 rounded w-3/4"></div>
                    </div>
                ))}
            </div>
        );
    }

    if (!summary) return null;

    const cards = [
        {
            title: '전체 사원',
            value: summary.totalEmployeeCount,
            unit: '명',
            icon: '👥',
            color: 'text-blue-600'
        },
        {
            title: '평균 HCROI',
            value: summary.averageHcroi?.toFixed(2) || '0',
            unit: '',
            icon: '📊',
            color: summary.averageHcroi >= 1 ? 'text-green-600' : 'text-red-600'
        },
        {
            title: '평균 점수',
            value: summary.averageScore?.toFixed(0) || '0',
            unit: '점',
            icon: '⭐',
            color: summary.averageScore >= 700 ? 'text-green-600' : 'text-yellow-600'
        },
        {
            title: '위험군 사원',
            value: summary.redZoneCount,
            unit: '명',
            icon: '⚠️',
            color: summary.redZoneCount > 0 ? 'text-red-600' : 'text-green-600'
        }
    ];

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {cards.map((card, index) => (
                <div key={index} className="card hover:shadow-xl transition-shadow">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500 dark:text-gray-400">{card.title}</p>
                            <p className={`text-3xl font-bold mt-2 ${card.color}`}>
                                {card.value}
                                <span className="text-lg font-normal ml-1">{card.unit}</span>
                            </p>
                        </div>
                        <span className="text-4xl">{card.icon}</span>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default SummaryCards;
