package com.appcloner.sandbox.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.appcloner.sandbox.core.VirtualEngineManager
import com.appcloner.sandbox.data.model.ClonedAppGroup
import com.appcloner.sandbox.data.model.HomeScreenAppItem
import com.appcloner.sandbox.data.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val engineManager = VirtualEngineManager.getInstance(context)

    /**
     * استخراج تمام برنامه‌های نصب شده روی گوشی با فیلتر اکتیویتی ورود
     */
    suspend fun getInstalledDeviceApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val currentPackage = context.packageName
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        resolveInfos
            .filter { it.activityInfo.packageName != currentPackage }
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val name = resolveInfo.loadLabel(packageManager).toString()
                val icon = resolveInfo.loadIcon(packageManager)
                InstalledApp(
                    packageName = pkg,
                    name = name,
                    icon = icon,
                    isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }

    /**
     * استخراج تک‌تک نسخه‌های کلون‌شده به عنوان آیتم‌های صفحه اصلی (Home Screen)
     * هر کلون به شکل یک برنامه مجزا با شماره (مثل «تلگرام ۱»، «تلگرام ۲») نمایش می‌یابد.
     */
    suspend fun getHomeScreenClonedItems(): List<HomeScreenAppItem> = withContext(Dispatchers.IO) {
        val clonedPackageNames = engineManager.getAllClonedPackageNames()
        val items = mutableListOf<HomeScreenAppItem>()

        for (pkg in clonedPackageNames) {
            val instances = engineManager.getInstalledInstances(pkg)
            if (instances.isNotEmpty()) {
                val appName: String
                val appIcon = try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    appName = packageManager.getApplicationLabel(appInfo).toString()
                    packageManager.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    appName = pkg
                    null
                }

                for (inst in instances.sortedBy { it.userId }) {
                    val number = inst.userId + 1
                    items.add(
                        HomeScreenAppItem(
                            packageName = pkg,
                            originalName = appName,
                            displayName = "$appName $number",
                            instanceNumber = number,
                            userId = inst.userId,
                            icon = appIcon
                        )
                    )
                }
            }
        }
        items
    }

    /**
     * ایجاد یک نسخه کلون جدید
     */
    suspend fun cloneApp(packageName: String): Result<Unit> {
        val res = engineManager.cloneApp(packageName)
        return if (res.isSuccess) Result.success(Unit) else Result.failure(res.exceptionOrNull() ?: Exception("Unknown error"))
    }

    /**
     * اجرای قطعی نسخه برنامه
     */
    fun launchInstance(callerContext: Context, packageName: String, userId: Int): Pair<Boolean, String?> {
        return engineManager.launchClonedApp(callerContext, packageName, userId)
    }

    /**
     * حذف نسخه
     */
    suspend fun deleteInstance(packageName: String, userId: Int): Boolean {
        return engineManager.removeCloneInstance(packageName, userId)
    }
}
