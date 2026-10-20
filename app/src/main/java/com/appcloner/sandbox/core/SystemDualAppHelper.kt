package com.appcloner.sandbox.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object SystemDualAppHelper {

    /**
     * باز کردن مستقیم صفحه تنظیمات شبیه‌ساز رسمی و کارخانه‌ای گوشی (سامسونگ، شیائومی، هواوی، اوپو و...)
     * بدون هیچ‌گونه دستکاری یا ورود به صفحه پاک‌سازی اطلاعات برنامه اصلی
     */
    fun openSystemDualAppSetting(context: Context, packageName: String): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()

        // 1. شیائومی / ردمی / پوکو (MIUI / HyperOS Dual Apps)
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.miui.securityadd", "com.miui.securityadd.dualapp.DualAppActivity")),
                Intent("miui.intent.action.DUAL_APP"),
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securityadd.dualapp.DualAppActivity")),
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.dualapp.ui.DualAppActivity"))
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "در لیست برنامه‌ها، کلید ایجاد نسخه کلون را روشن کنید", Toast.LENGTH_LONG).show()
                    return true
                } catch (e: Exception) {
                    // ادامه
                }
            }
        }

        // 2. سامسونگ (Samsung One UI Dual Messenger)
        if (manufacturer.contains("samsung")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.samsung.android.da.daagent", "com.samsung.android.da.daagent.MainActivity")),
                Intent().setComponent(ComponentName("com.samsung.android.da.daagent", "com.samsung.android.da.daagent.DualAppSettingsActivity")),
                Intent("com.samsung.android.da.daagent.action.DUAL_APP"),
                Intent("com.samsung.android.da.daagent.START_DUAL_APP"),
                Intent().setComponent(ComponentName("com.android.settings", "com.samsung.android.settings.dualapp.DualAppSettingsActivity")),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DualAppSettingsActivity")),
                Intent(Settings.ACTION_SYNC_SETTINGS)
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "در تنظیمات پیام‌رسان دوگانه (Dual Messenger)، کلید برنامه را فعال کنید", Toast.LENGTH_LONG).show()
                    return true
                } catch (e: Exception) {
                    // ادامه
                }
            }
        }

        // 3. هواوی و آنر (Huawei / Honor App Twin)
        if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$CloneSettingsActivity")),
                Intent().setComponent(ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.apptwin.AppTwinActivity"))
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "در App Twin کلید نسخه دوم برنامه را روشن کنید", Toast.LENGTH_LONG).show()
                    return true
                } catch (e: Exception) {}
            }
        }

        // 4. اوپو و ریلمی و وان‌پلاس (ColorOS / OxygenOS / Realme UI App Cloner)
        if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.coloros.clone", "com.coloros.clone.MainActivity")),
                Intent().setComponent(ComponentName("com.coloros.clone", "com.coloros.clone.CloneSettingsActivity")),
                Intent().setComponent(ComponentName("com.oplus.clone", "com.oplus.clone.MainActivity")),
                Intent().setComponent(ComponentName("com.oneplus.clone", "com.oneplus.clone.MainActivity"))
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "در بخش کلونر گوشی، برنامه را فعال نمایید", Toast.LENGTH_LONG).show()
                    return true
                } catch (e: Exception) {}
            }
        }

        // 5. ویوو (Vivo / iQOO App Clone)
        if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
            val intents = listOf(
                Intent().setComponent(ComponentName("com.vivo.doubleinstance", "com.vivo.doubleinstance.activity.DoubleInstanceActivity")),
                Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.clone.CloneActivity"))
            )
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {}
            }
        }

        // تنظیمات عمومی بدون دستکاری برنامه اصلی
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, "در تنظیمات گوشی، بخش شبیه‌ساز (Dual Apps / Dual Messenger) را باز کنید", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            false
        }
    }
}
