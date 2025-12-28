interface HeaderProps {
    activeTab: 'dashboard' | 'admin';
    onTabChange: (tab: 'dashboard' | 'admin') => void;
}

const Header = ({ activeTab, onTabChange }: HeaderProps) => {
    return (
        <header className="bg-white dark:bg-gray-800 shadow-md sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Logo */}
                    <div className="flex items-center gap-3">
                        <span className="text-2xl">📊</span>
                        <h1 className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                            ValuMetric
                        </h1>
                    </div>

                    {/* Navigation */}
                    <nav className="flex gap-1">
                        <button
                            onClick={() => onTabChange('dashboard')}
                            className={`px-4 py-2 rounded-lg font-medium transition-colors ${activeTab === 'dashboard'
                                    ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-200'
                                    : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700'
                                }`}
                        >
                            대시보드
                        </button>
                        <button
                            onClick={() => onTabChange('admin')}
                            className={`px-4 py-2 rounded-lg font-medium transition-colors ${activeTab === 'admin'
                                    ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-200'
                                    : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700'
                                }`}
                        >
                            관리자 설정
                        </button>
                    </nav>
                </div>
            </div>
        </header>
    );
};

export default Header;
