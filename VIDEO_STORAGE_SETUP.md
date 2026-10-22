# إعداد تخزين الفيديوهات بأقل تكلفة (Cloudflare R2)

المشروع يدعم S3-compatible object storage. الخيار الافتراضي الموصى به للفيديو هو Cloudflare R2: الخطة المجانية تشمل 10 GB-month و1M عملية كتابة و10M عملية قراءة شهرياً، ولا توجد رسوم نقل بيانات خارجة. الاستخدام الذي يتجاوز الحصة المجانية يُحاسب حسب أسعار R2 الحالية.

## إعداد الحساب مرة واحدة

1. أنشئ R2 bucket خاصاً (لا تفعّل Public access).
2. أنشئ R2 S3 API token بصلاحيات Object Read & Write لهذا الـ bucket فقط، ولا تمنحه صلاحيات على كل الباكيتات أو Data Catalog ما لم تكن مطلوبة.
3. من إعدادات البكت أضف CORS policy التالية، واستبدل الأصل بالنطاق الفعلي للواجهة:

```json
[
  {
    "AllowedOrigins": ["http://localhost:4200", "https://YOUR-APP-DOMAIN"],
    "AllowedMethods": ["GET", "PUT", "HEAD"],
    "AllowedHeaders": ["content-type", "x-amz-date", "x-amz-content-sha256", "authorization"],
    "ExposeHeaders": ["etag"],
    "MaxAgeSeconds": 3600
  }
]
```

## متغيرات بيئة الخلفية

للتشغيل المحلي انسخ `storage.env.example` إلى `.env` في جذر المشروع، ثم ضع مفاتيح R2 الجديدة في `.env`. يقرأ `start.ps1` و`run-backend.bat` الملف تلقائياً، و`.env` مستبعد من Git.

```text
VIDEO_S3_BUCKET=sports-recruitment
VIDEO_S3_ENDPOINT=https://39eb4146d755df0f751ffd9b61cfef7b.r2.cloudflarestorage.com
VIDEO_S3_REGION=auto
AWS_ACCESS_KEY_ID=مفتاح-R2-جديد
AWS_SECRET_ACCESS_KEY=سر-R2-جديد
VIDEO_S3_URL_TTL=3600
```

لا تضع المفاتيح في الواجهة الأمامية أو Git. في الإنتاج خزّنها في Secrets Manager أو إعداد الأسرار لدى مزود الاستضافة. يرفع المتصفح مباشرة إلى R2 عبر رابط PUT صالح لعشر دقائق؛ روابط المشاهدة موقّعة وصالحة افتراضياً ساعة واحدة. حد الملف 100 MB، والملفات الشخصية محدودة بثلاثة فيديوهات.

يمكن استخدام Amazon S3 بدلاً من R2 بترك `VIDEO_S3_ENDPOINT` فارغاً وتحديد `VIDEO_S3_REGION`، مع مراعاة أن S3 قد يفرض رسوم نقل بيانات خارجية بعد الحصة المجانية.
