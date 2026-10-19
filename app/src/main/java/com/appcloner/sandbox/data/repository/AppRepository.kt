package com.appcloner.sandbox.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.appcloner.sandbox.core.VirtualEngineManager
import com.appcloner.sandbox.data.model.ClonedAppGroup
import com.appcloner.sandbox.data.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val engineManager = VirtualEngineManager.getInstance(context)

    /**
     * استخراج تمام برنامه‌های نصب شده روی گوشی کاربر (به جز خود برنامه کلونر)
     */
    suspend fun getInstalledDeviceApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val currentPackage = context.packageName

        packages
            .filter { it.packageName != currentPackage }
            // فیلتر برنامه‌های دارای لانچر و صفحه ورود
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .map { appInfo ->
                InstalledApp(
                    packageName = appInfo.packageName,
                    name = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    /**
     * دریافت لیست برنامه‌هایی که در سندباکس کلون شده‌اند به همراه نسخه‌های فعالشان
     */
    suspend fun getClonedApps(): List<ClonedAppGroup> = withContext(Dispatchers.IO) {
        val clonedPackageNames = engineManager.getAllClonedPackageNames()
        val result = mutableListOf<ClonedAppGroup>()

        for (pkg in clonedPackageNames) {
            val instances = engineManager.getInstalledInstances(pkg)
            if (instances.isNotEmpty()) {
                try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    val name = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)

                    result.add(
                        ClonedAppGroup(
                            packageName = pkg,
                            name = name,
                            icon = icon,
                            instances = instances
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    // در صورتی که برنامه اصلی از سیستم حذف شده باشد
                    result.add(
                        ClonedAppGroup(
                            packageName = pkg,
                            name = pkg,
                            icon = null,
                            instances = instances
                        )
                    )
                }
            }
        }
        result
    }

    /**
     * ایجاد یک نسخه کلون جدید
     */
    suspend fun cloneApp(packageName: String): Result<Unit> {
        val res = engineManager.cloneApp(packageName)
        return if (res.isSuccess) Result.success(Unit) else Result.failure(res.exceptionOrNull() ?: Exception("Unknown error"))
    }

    /**
     * اجرای یک نسخه خاص
     */
    fun launchInstance(packageName: String, userId: Int): Boolean {
        return engineManager.launchClonedApp(packageName, userId)
    }

    /**
     * حذف یک نسخه خاص
     */
    suspend fun deleteInstance(packageName: String, userId: Int): Boolean {
        return engineManager.removeCloneInstance(packageName, userId)
    }
}
