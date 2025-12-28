# 📊 ValuMetric

> **HCROI 기반 인적자원 가치 측정 시스템**

인적자본 투자수익률(HCROI)을 활용하여 직원의 가치를 정량적으로 측정하고 관리하는 대시보드 애플리케이션입니다.

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 🎯 **HCROI 대시보드** | 전사 인적자원 현황 실시간 모니터링 |
| ⚠️ **Red Zone 관리** | 저성과 직원 조기 경보 시스템 |
| 🏆 **Top Performers** | 우수 성과자 추적 및 표창 |
| 📈 **추세 분석** | 매출/인건비 추이 차트 |
| 🎂 **생일 알림** | 다가오는 생일자 사이드바 |
| 🔐 **인증 시스템** | 로그인/회원가입/JWT 토큰 관리 |

---

## 🛠 기술 스택

### Backend
- **Java 17** + **Spring Boot 3.4**
- **Spring Data MongoDB**
- **Spring Security** + **JWT (jjwt)**
- **SpringDoc OpenAPI** (Swagger UI)

### Frontend
- **React 18** + **TypeScript**
- **Vite**
- **TailwindCSS**
- **Chart.js / Recharts**

### Database
- **MongoDB**

---

## 🚀 시작하기

### 1. 요구사항
- Java 17+
- Node.js 18+
- MongoDB (로컬 또는 Atlas)

### 2. 환경 변수 설정

```bash
# .env.example을 복사
cp .env.example .env

# .env 파일 수정
MONGODB_URI=mongodb://localhost:27017/valumetric
JWT_SECRET=your-super-secret-key-at-least-256-bits
```

### 3. 백엔드 실행

```bash
# Windows
.\gradlew.bat bootRun

# Mac/Linux
./gradlew bootRun
```

### 4. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

### 5. 접속
- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html

---

## 📁 프로젝트 구조

```
ValuMetric/
├── src/main/java/com/valumetric/
│   ├── config/          # 설정 (Security, CORS, DataInit)
│   ├── controller/      # REST API 컨트롤러
│   ├── document/        # MongoDB Document
│   ├── dto/             # Data Transfer Objects
│   ├── repository/      # MongoDB Repository
│   ├── security/        # JWT, UserDetails
│   ├── service/         # 비즈니스 로직
│   └── calculator/      # HCROI 계산 엔진
├── frontend/
│   ├── src/
│   │   ├── components/  # React 컴포넌트
│   │   ├── api/         # API 클라이언트
│   │   └── types/       # TypeScript 타입
│   └── public/          # PWA 에셋
└── .env.example         # 환경변수 템플릿
```

---

## 📊 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/register` | 회원가입 |
| GET | `/api/dashboard` | 대시보드 전체 데이터 |
| GET | `/api/dashboard/birthdays` | 다가오는 생일 목록 |
| GET | `/api/employees` | 직원 목록 |
| GET | `/api/employees/{id}` | 직원 상세 |

---

## 🔐 보안

- `.env` 파일은 Git에서 제외됨
- JWT 토큰 기반 인증
- 비밀번호 BCrypt 암호화

---

## 📱 PWA 지원

앱 설치 가능 (Progressive Web App)
- Chrome: 주소창 ⊕ 아이콘 클릭
- 모바일: "홈 화면에 추가"

---

## 📄 라이선스

MIT License

---

## 👨‍💻 개발자

- GitHub: [@hongseongug812-ui](https://github.com/hongseongug812-ui)
