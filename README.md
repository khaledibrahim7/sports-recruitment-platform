# الكورة لعبتنا — منصة استكشاف وتكشيف المواهب الرياضية (Sports Talent Recruitment Platform)

منظومة رقمية متكاملة تربط **اللاعبين**، **الأكاديميات الرياضية**، و**الأندية** لتوثيق السجلات البدنية والفنية، استعراض المقاطع التحليلية، وتنسيق فرص المعايشة والاختبارات الميدانية.

---

## المعمارية التقنية (Tech Stack)

### 1. الواجهة الأمامية (`frontend/`)
- **الإطار:** Angular 19 (Standalone Components, Signals, `@defer`, Angular SSR).
- **التصميم:** نظام تصميم Editorial Dark Luxury مخصص مع خطوط `@fontsource` مستضافة محلياً (`Cairo`, `Playfair Display`, `Hanken Grotesk`) ودعم كامل للاتجاه `RTL` والوصولية (`ARIA` & Keyboard Navigation).
- **الأمان:** جلسات `HttpOnly` Cookies مع تجديد تلقائي (`refreshSession`) وحماية المسارات بـ `authGuard` و`roleGuard` و`guestGuard`، وفلترة صارمة لروابط الفيديو (`YouTube`, `Vimeo`, `Google Drive`, `MP4`).
- **المراقبة والاختبارات:** تكامل `Sentry` لمعالجة الأخطاء + اختبارات وحدات (`Karma/Jasmine`) واختبارات شاملة (`Playwright`).

### 2. الخادم الخلفي (`backend/`)
- **الإطار:** Java 17 + Spring Boot 3 (بنية متعددة الوحدات Multi-Module Maven).
- **الوحدات:**
  - `sports-common`: الأمان (`Spring Security`, `JWT`, `RateLimitingFilter`, `CSP Headers`)، الإشعارات، والاستثناءات الموحدة.
  - `sports-feature-auth`: التسجيل الآمن، تأكيد البريد الإلكتروني، وموافقة ولي الأمر للقُصّر.
  - `sports-feature-player`: ملفات اللاعبين، التصفية والبحث المرقّم (Server-Side Pagination)، دعوات المعايشة، وقائمة متابعة الكشافين.
  - `sports-feature-academy` & `sports-feature-club`: إدارة وعرض ملفات الأكاديميات والأندية.
  - `sports-feature-ai`: المساعد الذكي للتكشيف الرياضي («كاشف AI»).
  - `sports-web`: نقاط النهاية (`REST Controllers`)، إدارة الجلسات (`RefreshSessionService`)، وتخزين الوسائط (`S3/MinIO`).

---

## التشغيل السريع (Quick Start)

### المتطلبات المسبقة
- **Java 17+** و **Maven 3.9+**
- **Node.js 20+** و **npm**
- **PostgreSQL** (أو استخدام الإعداد المحلي `application-local.yml`)

### تشغيل الـ Backend
```bash
mvn -f backend/pom.xml spring-boot:run -pl sports-web
```

### تشغيل الـ Frontend
```bash
cd frontend
npm ci
npm start
```
سيعمل التطبيق على `http://localhost:4200`.

### الفحص والاختبارات (Verification & Quality Gates)
```bash
# فحص الأنواع (TypeScript)
npm --prefix frontend run typecheck

# اختبارات الوحدات للواجهة الأمامية
npm --prefix frontend run test:ci

# بناء الإنتاج مع SSR
npm --prefix frontend run build

# بناء واختبار الـ Backend
mvn -B -f backend/pom.xml verify
```
