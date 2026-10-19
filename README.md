# App Cloner Sandbox (پروژه اندروید کلونر با محیط مجازی)

یک اپلیکیشن بومی اندروید مدرن نوشته شده با **Kotlin** و **Jetpack Compose** که به شما امکان می‌دهد از هر برنامه‌ای که روی گوشی دارید، به تعداد نامحدود نسخه شبیه‌سازی‌شده (Clone) ایجاد کنید و هر کدام را با اکانت، دیتا و حافظه کاملاً مستقل اجرا نمایید.

---

## 🚀 ویژگی‌های کلیدی
1. **معماری Virtual Sandbox (شبیه BlackBox و Parallel Space):**
   - بدون نیاز به روت (No Root Needed).
   - بدون نیاز به تغییر فایل APK اصلی یا نصب مجدد خارج از سندباکس.
   - هر نسخه در یک محیط ایزوله و پروسه اختصاصی با شناسه `UserId` مجزا اجرا می‌شود.
2. **ساخت بی‌نهایت نسخه از یک برنامه:**
   - می‌توانید از تلگرام، واتساپ، اینستاگرام یا بازی‌ها چندین نسخه (نسخه ۱، نسخه ۲، نسخه ۳ و...) داشته باشید.
3. **رابط کاربری پیشرفته و روان (Jetpack Compose + Material 3):**
   - طراحی تیره (Dark Mode) و چشم‌نواز با رنگ‌بندی مدرن نئونی.
   - تب تفکیک‌شده برای «برنامه‌های کلون‌شده» و «تمام برنامه‌های گوشی».
   - جستجوی آنی (Live Search) بر اساس نام یا پکیج.
   - دیالوگ مدیریت نسخه‌ها با امکان اجرای مستقیم یا حذف هر نسخه دلخواه.

---

## 🏗️ ساختار پروژه

```
android-cloner/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/values/ (strings, colors, themes)
│       └── java/com/appcloner/sandbox/
│           ├── AppClonerApplication.kt          # مقداردهی هسته مجازی‌سازی و هوک سیستم
│           ├── core/
│           │   └── VirtualEngineManager.kt      # مدیریت فراخوانی‌های هسته کانتینر و User ID
│           ├── data/
│           │   ├── model/AppModel.kt            # مدل‌های برنامه‌ها و اینستنس‌ها
│           │   └── repository/AppRepository.kt  # ارتباط با پکیج منیجر و استخراج آیکون‌ها
│           └── ui/
│               ├── MainActivity.kt              # ورودی اصلی برنامه
│               ├── components/
│               │   ├── AppCard.kt               # کارت‌های نمایش برنامه
│               │   └── InstanceManagementDialog.kt # پاپ‌آپ مدیریت نسخه‌ها
│               ├── screens/
│               │   └── HomeScreen.kt            # صفحه اصلی با سرچ و تب‌ها
│               └── theme/                       # پالت رنگ و تم مدرن Compose
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠️ نحوه باز کردن و کامپایل در Android Studio

1. نرم‌افزار **Android Studio** را باز کنید.
2. گزینه **Open** را انتخاب کرده و پوشه `android-cloner` را باز کنید.
3. منتظر بمانید تا فرایند **Gradle Sync** انجام شود.
4. گوشی موبایل خود را با کابل USB (حالت Developer Options و USB Debugging روشن) متصل کرده یا یک شبیه‌ساز (Emulator) اجرا کنید.
5. دکمه **Run (Shift + F10)** را بزنید تا برنامه روی گوشی نصب و اجرا شود.

### ساخت خروجی APK از طریق ترمینال:
```bash
cd android-cloner
./gradlew assembleDebug
```
فایل نهایی در مسیر زیر تولید خواهد شد:
`app/build/outputs/apk/debug/app-debug.apk`

---

## ⚙️ نحوه عملکرد فنی موتور مجازی‌سازی (Virtual Sandbox Core)

این سیستم با ایجاد یک کانتینر سطح بالا روی فریم‌ورک اندروید کار می‌کند:
1. **Binder Hooking:** متدهای سیستمی مانند `ActivityManagerService` و `PackageManagerService` هوک می‌شوند تا سیستم‌عامل متوجه شود پکیج در حال اجرا درون یک دایرکتوری داده مجزا (`/data/data/.../virtual/users/<id>/`) فعالیت می‌کند.
2. **Multi-User Mapping:** هر نمونه شبیه‌سازی یک شناسه کاربری (User ID) منحصر به فرد مانند `0`, `1`, `2` دریافت می‌کند؛ بنابراین کوکی‌ها، کش، دیتابیس SQLite و SharedPreferences هیچ‌کدام با یکدیگر یا با نسخه اصلی تداخل نخواهند داشت.
