import { useState, useEffect } from 'react';
import { Header, SummaryCards, RedZoneTable, EmployeeDetailModal, AdminSettingsPage } from './components';
import EmployeeManagementPage from './components/admin/EmployeeManagementPage';
import PerformanceInputPage from './components/admin/PerformanceInputPage';
import TopPerformersTable from './components/dashboard/TopPerformersTable';
import RevenueLaborTrendChart from './components/charts/RevenueLaborTrendChart';
import BepGauge from './components/dashboard/BepGauge';
import AhpWeightsRadar from './components/charts/AhpWeightsRadar';
import WatchListTable from './components/dashboard/WatchListTable';
import LoginPage from './components/auth/LoginPage';
import SignUpPage from './components/auth/SignUpPage';
import UpcomingBirthdaysSidebar from './components/dashboard/UpcomingBirthdaysSidebar';
import { dashboardApi } from './api';
import type { DashboardSummary, RedZoneEmployee, MonthlyTrendData, TopPerformer, WatchListEmployee } from './types';
import type { EmployeeDetail } from './types/employee';

type TabType = 'dashboard' | 'employees' | 'performance' | 'admin';
type AuthPageType = 'login' | 'signup';

function App() {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard');
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [redZoneEmployees, setRedZoneEmployees] = useState<RedZoneEmployee[]>([]);
  const [topPerformers, setTopPerformers] = useState<TopPerformer[]>([]);
  const [watchList, setWatchList] = useState<WatchListEmployee[]>([]);

  // 인증 상태
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentUser, setCurrentUser] = useState<{ id: string; name: string; role: string; companyName?: string } | null>(null);
  const [authPage, setAuthPage] = useState<AuthPageType>('login');

  // 모달 상태
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedEmployeeDetail, setSelectedEmployeeDetail] = useState<EmployeeDetail | null>(null);
  const [selectedEmployeeTrend, setSelectedEmployeeTrend] = useState<MonthlyTrendData[]>([]);

  // 로그인 상태 확인
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setCurrentUser(JSON.parse(storedUser));
      setIsLoggedIn(true);
    }
  }, []);

  // 대시보드 데이터 로드
  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      try {
        const response = await dashboardApi.getDashboard();
        setSummary(response.data.summary);
        setRedZoneEmployees(response.data.redZoneEmployees || []);
        setTopPerformers(response.data.topPerformers || []);
        setWatchList(response.data.watchList || []);
      } catch (error) {
        console.error('대시보드 데이터 로드 실패:', error);
        setSummary({
          totalEmployeeCount: 0,
          averageHcroi: 0,
          averageScore: 0,
          redZoneCount: 0,
          unresolvedAlertCount: 0,
          companyTotalRevenue: 0
        });
        setRedZoneEmployees([]);
      } finally {
        setLoading(false);
      }
    };

    if (activeTab === 'dashboard' && isLoggedIn) {
      fetchDashboardData();
    }
  }, [activeTab, isLoggedIn]);

  // 사원 클릭 시 상세 모달 열기
  const handleEmployeeClick = async (employeeId: string | number) => {
    const employee = redZoneEmployees.find(e => e.employeeId === employeeId);
    if (!employee) return;

    try {
      const response = await dashboardApi.getEmployeeTrend(String(employeeId));
      setSelectedEmployeeTrend(response.data.trendData);
    } catch (error) {
      console.error('추이 데이터 로드 실패:', error);
      setSelectedEmployeeTrend([]);
    }

    const breakEvenPointSales = employee.currentSalary / 12 * 1.5;
    const currentRevenue = breakEvenPointSales * (employee.targetAchievementRate / 100);

    const detail: EmployeeDetail = {
      employeeId: employee.employeeId,
      employeeName: employee.employeeName,
      currentGrade: employee.currentGrade,
      currentSalary: employee.currentSalary,
      hireDate: '',
      currentHcroi: employee.currentHcroi,
      targetAchievementRate: employee.targetAchievementRate,
      breakEvenPointSales,
      currentRevenue,
      remainingToBreakEven: Math.max(0, breakEvenPointSales - currentRevenue),
      currentScore: employee.currentScore,
      scoreGrade: employee.currentScore >= 900 ? 'S' :
        employee.currentScore >= 800 ? 'A' :
          employee.currentScore >= 700 ? 'B' :
            employee.currentScore >= 600 ? 'C' : 'D',
      unresolvedAlertCount: employee.unresolvedAlertCount,
      riskLevel: employee.riskLevel
    };

    setSelectedEmployeeDetail(detail);
    setSelectedEmployeeTrend([]);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedEmployeeDetail(null);
    setSelectedEmployeeTrend([]);
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return (
          <div className="space-y-8">
            <SummaryCards summary={summary} loading={loading} />
            <BepGauge />
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2">
                <RevenueLaborTrendChart />
              </div>
              <div>
                <AhpWeightsRadar />
              </div>
            </div>
            <TopPerformersTable
              performers={topPerformers}
              onEmployeeClick={handleEmployeeClick}
            />
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <RedZoneTable
                employees={redZoneEmployees}
                onEmployeeClick={handleEmployeeClick}
              />
              <WatchListTable
                watchList={watchList}
                onEmployeeClick={handleEmployeeClick}
              />
            </div>
          </div>
        );
      case 'employees':
        return <EmployeeManagementPage />;
      case 'performance':
        return <PerformanceInputPage />;
      case 'admin':
        return <AdminSettingsPage />;
      default:
        return null;
    }
  };

  // 로그인 성공 핸들러
  const handleLoginSuccess = (user: { id: string; name: string; role: string; companyName?: string }) => {
    setCurrentUser(user);
    setIsLoggedIn(true);
  };

  // 로그아웃
  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setCurrentUser(null);
    setIsLoggedIn(false);
  };

  // 로그인 안 된 경우 로그인/회원가입 페이지 표시
  if (!isLoggedIn) {
    if (authPage === 'signup') {
      return (
        <SignUpPage
          onSignUpSuccess={() => setAuthPage('login')}
          onBackToLogin={() => setAuthPage('login')}
        />
      );
    }
    return (
      <LoginPage
        onLoginSuccess={handleLoginSuccess}
        onSignUp={() => setAuthPage('signup')}
      />
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* 네비게이션 */}
      <nav className="bg-white dark:bg-gray-800 shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center gap-3">
              <h1 className="text-xl font-bold text-blue-600">📊 ValuMetric</h1>
              {currentUser?.companyName && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 text-sm font-medium rounded">
                  🏢 {currentUser.companyName}
                </span>
              )}
            </div>
            <div className="flex items-center space-x-1">
              <button
                onClick={() => setActiveTab('dashboard')}
                className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'dashboard'
                  ? 'bg-blue-500 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
                  }`}
              >
                대시보드
              </button>
              <button
                onClick={() => setActiveTab('employees')}
                className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'employees'
                  ? 'bg-blue-500 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
                  }`}
              >
                👥 사원관리
              </button>
              <button
                onClick={() => setActiveTab('performance')}
                className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'performance'
                  ? 'bg-blue-500 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
                  }`}
              >
                📈 실적입력
              </button>
              <button
                onClick={() => setActiveTab('admin')}
                className={`px-4 py-2 rounded-lg font-medium ${activeTab === 'admin'
                  ? 'bg-blue-500 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
                  }`}
              >
                ⚙️ 설정
              </button>
            </div>
            {/* 사용자 정보 & 로그아웃 */}
            <div className="flex items-center gap-3">
              <span className="text-sm text-gray-600 dark:text-gray-300">
                👤 {currentUser?.name || '사용자'}
              </span>
              <button
                onClick={handleLogout}
                className="px-3 py-1.5 text-sm text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition"
              >
                로그아웃
              </button>
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {renderContent()}
      </main>

      {/* 사원 상세 모달 */}
      <EmployeeDetailModal
        employee={selectedEmployeeDetail}
        trendData={selectedEmployeeTrend}
        isOpen={isModalOpen}
        onClose={handleCloseModal}
      />

      {/* 생일 사이드바 */}
      <UpcomingBirthdaysSidebar />

      {/* Footer */}
      <footer className="border-t border-gray-200 dark:border-gray-800 py-6 mt-12">
        <p className="text-center text-gray-500 text-sm">
          © 2024 ValuMetric - HCROI 기반 인적자본 관리 시스템
        </p>
      </footer>
    </div>
  );
}

export default App;
