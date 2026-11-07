# الكورة لعبتنا — Frontend

واجهة المنصة مبنية بـ Angular 19 وTypeScript. التطبيق SPA مع تحميل كسول للصفحات، ويعتمد على Spring Boot API تحت `/api/v1`.

## المتطلبات

- Node.js 20 أو أحدث
- npm
- backend محلي يعمل على `http://localhost:8080` عند اختبار التكامل

## التشغيل المحلي

```powershell
npm ci
npm start
```

يفتح Angular على `http://localhost:4200` ويستخدم [proxy.conf.json](proxy.conf.json) لإرسال `/api` إلى backend المحلي.

## التحقق والبناء

```powershell
npm run typecheck
npm run test:ci
npm run test:e2e
npm run build
```

`typecheck` يتحقق من كود التطبيق والاختبارات. `test:ci` يشغل Jasmine/Karma على ChromeHeadless. ناتج البناء الإنتاجي يوجد في `dist/frontend/browser`.

لتشغيل E2E محليًا، نزّل Chromium مرة واحدة باستخدام `npx playwright install chromium` ثم شغّل `npm run test:e2e`.

## النشر

ملف GitHub Actions في `../.github/workflows/deploy.yml` ينشر فرع `prod` إلى GitHub Pages. الصفحات العامة تُ prerender إلى HTML ثابت، و`404.html` يوفر fallback للروابط العميقة. الواجهة تستدعي `/api/v1` على نفس النطاق؛ لذلك يحتاج نشر الإنتاج إلى reverse proxy أو gateway يمرر هذه المسارات إلى backend، مع إعداد CORS وCSP والتخزين وفق ملفات التشغيل في جذر المستودع. GitHub Pages وحده لا يشغّل backend ولا SSR وقت الطلب.

## الإعدادات والبيانات الحساسة

لا تضع مفاتيح التخزين أو البريد أو JWT في ملفات الواجهة أو Git. إعدادات backend المحلية موثقة في ملفات الإعداد بجذر المشروع، ويجب تمرير أسرار الإنتاج من مدير أسرار بيئة النشر.

راقب الأخطاء عبر Sentry بعد تعريف متغير GitHub Actions `SENTRY_DSN` (Repository Variables). الـ DSN معرف عام للواجهة؛ لا تستخدم فيه أسرارًا.
