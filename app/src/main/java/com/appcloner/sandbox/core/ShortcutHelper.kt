package com.appcloner.sandbox.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.appcloner.sandbox.ui.MainActivity

object ShortcutHelper {

    /**
     * افزودن میانبر برنامه شبیه‌سازی شده مستقیم به صفحه اصلی گوشی
     */
    fun createHomeScreenShortcut(
        context: Context,
        packageName: String,
        appName: String,
        instanceNumber: Int,
        iconDrawable: Drawable?
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_PACKAGE_NAME", packageName)
            putExtra("EXTRA_USER_ID", instanceNumber - 1)
        }
        launchIntent.action = Intent.ACTION_MAIN
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        // تبدیل دراویبل به بیتمپ برای آیکون میانبر
        val bitmap = iconDrawable?.let { d ->
            val w = if (d.intrinsicWidth > 0) d.intrinsicWidth else 128
            val h = if (d.intrinsicHeight > 0) d.intrinsicHeight else 128
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            d.setBounds(0, 0, canvas.width, canvas.height)
            d.draw(canvas)
            bmp
        }

        val shortcutLabel = "$appName $instanceNumber"
        val shortcutId = "clone_${packageName}_$instanceNumber"

        val shortcutBuilder = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(shortcutLabel)
            .setLongLabel(shortcutLabel)
            .setIntent(launchIntent)

        if (bitmap != null) {
            shortcutBuilder.setIcon(IconCompat.createWithBitmap(bitmap))
        }

        val shortcutInfo = shortcutBuilder.build()
        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
