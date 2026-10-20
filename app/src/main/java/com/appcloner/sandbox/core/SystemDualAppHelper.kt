package com.appcloner.sandbox.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object SystemDualAppHelper {

    /**
     * باز کردن مستقیم صفحه تنظیمات شبیه‌ساز کارخانه‌ای گوشی (سامسونگ، شیائومی، هواوی و...)
     */
    fun openSystemDualAppSetting(context: Context, packageName: String): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()

        // 1. شیائومی / ردمی / پوکو (Dual Apps)
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.miui.securityadd", "com.miui.securityadd.dualapp.DualAppActivity")),
                Intent("miui.intent.action.DUAL_APP"),
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securityadd.dualapp.DualAppActivity"))
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    // ادامه به تلاش بعدی
                }
            }
        }

        // 2. سامسونگ (Dual Messenger)
        if (manufacturer.contains("samsung")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.samsung.android.da.daagent", "com.samsung.android.da.daagent.MainActivity")),
                Intent("com.samsung.android.da.daagent.action.DUAL_APP"),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DualAppSettingsActivity"))
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    // ادامه
                }
            }
        }

        // 3. هواوی و آنر (App Twin)
        if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
            try {
                val intent = Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$CloneSettingsActivity"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {}
        }

        // 4. اوپو و ریلمی (App Cloner)
        if (manufacturer.contains("oppo") || manufacturer.contains("realme")) {
            try {
                val intent = Intent().setComponent(ComponentName("com.coloros.clone", "com.coloros.clone.MainActivity"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {}
        }

        // در صورت عدم پشتیبانی یا برند دیگر، هدایت به صفحه تنظیمات برنامه
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
