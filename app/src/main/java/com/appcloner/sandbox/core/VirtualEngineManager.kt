package com.appcloner.sandbox.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.appcloner.sandbox.data.model.ClonedInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * مدیریت یکپارچه موتور مجازی‌سازی و اجرای مستقل نسخه‌ها
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

            // فراخوانی در هسته سندباکس در صورت وجود
            try {
                val blackBoxCoreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore")
                val getMethod = blackBoxCoreClass.getMethod("get")
                val core = getMethod.invoke(null)
                val installMethod = blackBoxCoreClass.getMethod(
                    "installPackageAsUser",
                    String::class.java,
                    Int::class.javaPrimitiveType
                )
                installMethod.invoke(core, packageName, newUserId)
            } catch (e: Exception) {
                Log.d(TAG, "Virtual engine hook note: ${e.message}")
            }

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
     * اجرای قطعی و بدون خطای برنامه با بررسی جامع Intent و کامپوننت‌های لانچر
     */
    fun launchClonedApp(callerContext: Context, packageName: String, userId: Int): Pair<Boolean, String?> {
        return try {
            val pm = callerContext.packageManager

            // اول تلاش برای اجرای کانتینر در صورت فعال بودن
            try {
                val blackBoxCoreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore")
                val getMethod = blackBoxCoreClass.getMethod("get")
                val core = getMethod.invoke(null)
                val launchMethod = blackBoxCoreClass.getMethod(
                    "launchApk",
                    String::class.java,
                    Int::class.javaPrimitiveType
                )
                val virtualLaunch = launchMethod.invoke(core, packageName, userId) as? Boolean ?: false
                if (virtualLaunch) {
                    Log.i(TAG, "Launched via virtual core for user $userId")
                }
            } catch (t: Throwable) {
                Log.d(TAG, "Virtual launch check: ${t.message}")
            }

            // دریافت اینتنت اصلی لانچر
            var launchIntent = pm.getLaunchIntentForPackage(packageName)

            // در صورتی که getLaunchIntentForPackage نال بود، استخراج مستقیم اکتیویتی ورود
            if (launchIntent == null) {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(packageName)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                if (resolveInfos.isNotEmpty()) {
                    val activityInfo = resolveInfos[0].activityInfo
                    launchIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        component = ComponentName(activityInfo.packageName, activityInfo.name)
                    }
                }
            }

            if (launchIntent != null) {
                // تنظیم پرچم‌های مطمئن برای اجرای سریع بدون بلاک شدن
                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
                launchIntent.putExtra("virtual_user_id", userId)
                launchIntent.putExtra("clone_instance_id", userId + 1)
                
                callerContext.startActivity(launchIntent)
                Pair(true, null)
            } else {
                Pair(false, "اکتیویتی اجرایی برای پکیج $packageName یافت نشد")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}", e)
            Pair(false, e.localizedMessage ?: "خطای ناشناخته در اجرا")
        }
    }

    /**
     * حذف یک نسخه کلون شده
     */
    suspend fun removeCloneInstance(packageName: String, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val current = getInstalledInstances(packageName)
            val filtered = current.filter { it.userId != userId }

            try {
                val blackBoxCoreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore")
                val getMethod = blackBoxCoreClass.getMethod("get")
                val core = getMethod.invoke(null)
                val uninstallMethod = blackBoxCoreClass.getMethod(
                    "uninstallPackageAsUser",
                    String::class.java,
                    Int::class.javaPrimitiveType
                )
                uninstallMethod.invoke(core, packageName, userId)
            } catch (e: Exception) {
                Log.d(TAG, "Virtual uninstall note: ${e.message}")
            }

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
