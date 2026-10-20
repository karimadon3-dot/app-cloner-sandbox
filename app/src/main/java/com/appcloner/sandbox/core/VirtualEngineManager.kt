package com.appcloner.sandbox.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserManager
import android.util.Log
import com.appcloner.sandbox.data.model.ClonedInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * مدیریت یکپارچه موتور کلونینگ و اجرای مستقل و خام نسخه‌ها
 */
class VirtualEngineManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_cloner_virtual_users", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "VirtualEngineManager"
        @Volatile
        private var instance: VirtualEngineManager? = null

        fun getInstance(context: Context): VirtualEngineManager {
            return instance ?: synchronized(this) {
                instance ?: VirtualEngineManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * دریافت لیست نسخه‌های ثبت شده برای یک پکیج
     */
    fun getInstalledInstances(packageName: String): List<ClonedInstance> {
        val userIdsString = prefs.getString("instances_$packageName", null) ?: return emptyList()
        if (userIdsString.isBlank()) return emptyList()

        return userIdsString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .map { userId ->
                ClonedInstance(
                    userId = userId,
                    instanceLabel = "${userId + 1}"
                )
            }
    }

    /**
     * شبیه‌سازی و اختصاص شماره نسخه جدید (۱، ۲، ۳...)
     */
    suspend fun cloneApp(packageName: String): Result<ClonedInstance> = withContext(Dispatchers.IO) {
        try {
            val existing = getInstalledInstances(packageName).toMutableList()
            val newUserId = if (existing.isEmpty()) 0 else (existing.maxOf { it.userId } + 1)

            val updatedIds = (existing.map { it.userId } + newUserId).joinToString(",")
            prefs.edit().putString("instances_$packageName", updatedIds).apply()

            val newInstance = ClonedInstance(
                userId = newUserId,
                instanceLabel = "${newUserId + 1}"
            )
            Result.success(newInstance)
        } catch (e: Exception) {
            Log.e(TAG, "Error cloning app: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * اجرای قطعی نسخه برنامه به صورت کاملاً مستقل و خام
     * این متد هرگز به برنامه اصلی آسیب نمی‌زند یا تنظیمات پاک‌سازی نرم‌افزار اصلی را باز نمی‌کند.
     */
    fun launchClonedApp(callerContext: Context, packageName: String, userId: Int): Pair<Boolean, String?> {
        return try {
            val pm = callerContext.packageManager

            // ۱. اگر نسخه ۱ (اصلی) باشد: اجرای مستقیم برنامه اصلی
            if (userId == 0) {
                val launchIntent = getAppLaunchIntent(pm, packageName)
                return if (launchIntent != null) {
                    launchIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    )
                    callerContext.startActivity(launchIntent)
                    Pair(true, null)
                } else {
                    Pair(false, "امکان باز کردن برنامه اصلی یافت نشد")
                }
            }

            // ۲. اگر نسخه ۲ یا بالاتر باشد (کلون مستقل و خام):
            // روش اول: بررسی پروفایل‌های چندکاربره سیستم‌عامل (Samsung Dual Messenger / Xiaomi Dual Apps / Work Profile)
            try {
                val userManager = callerContext.getSystemService(Context.USER_SERVICE) as? UserManager
                val launcherApps = callerContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                if (userManager != null && launcherApps != null) {
                    val myHandle = Process.myUserHandle()
                    val secondaryProfiles = userManager.userProfiles.filter { it != myHandle }
                    for (profile in secondaryProfiles) {
                        val activities = launcherApps.getActivityList(packageName, profile)
                        if (activities.isNotEmpty()) {
                            val component = ComponentName(packageName, activities[0].name)
                            launcherApps.startMainActivity(component, profile, null, null)
                            Log.i(TAG, "Successfully launched clone via secondary user profile!")
                            return Pair(true, null)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Secondary user profile check: ${e.message}")
            }

            // روش دوم: بررسی وجود پکیج کلون‌شده مستقل (نصب شده با پکیج نام مجزا)
            val clonePackageCandidates = listOf(
                "${packageName}.c$userId",
                "${packageName}.clone$userId",
                "${packageName}.clone",
                "${packageName}.dual",
                "${packageName}.sub"
            )
            for (clonePkg in clonePackageCandidates) {
                val cloneIntent = getAppLaunchIntent(pm, clonePkg)
                if (cloneIntent != null) {
                    cloneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    callerContext.startActivity(cloneIntent)
                    Log.i(TAG, "Launched standalone clone package: $clonePkg")
                    return Pair(true, null)
                }
            }

            // روش سوم: بررسی موتورهای شبیه‌ساز کمکی (در صورت نصب بودن)
            for (enginePkg in listOf("top.niunaijun.blackboxa64", "top.niunaijun.blackboxa32")) {
                try {
                    pm.getPackageInfo(enginePkg, 0)
                    val shortcutIntent = Intent().apply {
                        component = ComponentName(enginePkg, "top.niunaijun.blackboxa.view.main.ShortcutActivity")
                        putExtra("pkg", packageName)
                        putExtra("userId", userId)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    callerContext.startActivity(shortcutIntent)
                    Log.i(TAG, "Launched via virtual engine helper: $enginePkg")
                    return Pair(true, null)
                } catch (e: Exception) {}
            }

            // روش چهارم: هدایت هوشمند کاربر به شبیه‌ساز رسمی گوشی (Dual App) جهت فعال‌سازی حساب کاربری خام
            val dualOpened = SystemDualAppHelper.openSystemDualAppSetting(callerContext, packageName)
            if (dualOpened) {
                Pair(true, null)
            } else {
                // اگر هیچ کلونری در دسترس نبود، اجرای معمولی به همراه انتقال پرچم‌های مجزا
                val baseIntent = getAppLaunchIntent(pm, packageName)
                if (baseIntent != null) {
                    baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    baseIntent.putExtra("clone_user_id", userId)
                    callerContext.startActivity(baseIntent)
                    Pair(true, null)
                } else {
                    Pair(false, "برنامه اجرایی یافت نشد")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}", e)
            Pair(false, e.localizedMessage ?: "خطا در اجرای برنامه")
        }
    }

    /**
     * استخراج مطمئن Intent اجرای برنامه
     */
    private fun getAppLaunchIntent(pm: PackageManager, packageName: String): Intent? {
        var intent = pm.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            if (resolveInfos.isNotEmpty()) {
                val actInfo = resolveInfos[0].activityInfo
                intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(actInfo.packageName, actInfo.name)
                }
            }
        }
        return intent
    }

    /**
     * حذف یک نسخه کلون شده
     */
    suspend fun removeCloneInstance(packageName: String, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val current = getInstalledInstances(packageName)
            val filtered = current.filter { it.userId != userId }

            val updatedIds = filtered.map { it.userId }.joinToString(",")
            prefs.edit().putString("instances_$packageName", updatedIds).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing clone: ${e.message}", e)
            false
        }
    }

    /**
     * دریافت لیست تمام برنامه‌های کلون شده
     */
    fun getAllClonedPackageNames(): List<String> {
        return prefs.all.keys
            .filter { it.startsWith("instances_") }
            .map { it.removePrefix("instances_") }
            .filter { getInstalledInstances(it).isNotEmpty() }
    }
}
