package com.appcloner.sandbox.data.model

import android.graphics.drawable.Drawable

/**
 * مدل برنامه‌های نصب شده روی دستگاه
 */
data class InstalledApp(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false,
    val versionName: String = ""
)

/**
 * مدل نسخه کپی مستقل داخل سندباکس
 */
data class ClonedInstance(
    val userId: Int,
    val instanceLabel: String,
    val isRunning: Boolean = false
)

/**
 * مدل نمایانگر هر آیکون در صفحه هوم گوشی کلونر
 * دقیقاً مثل یک صفحه هوم گوشی با نام و شماره نسخه (شماره ۱، ۲، ۳، ۴)
 */
data class HomeScreenAppItem(
    val packageName: String,
    val originalName: String,
    val displayName: String,    // مثال: "تلگرام ۱" یا "واتساپ ۲"
    val instanceNumber: Int,     // 1, 2, 3, 4
    val userId: Int,             // 0, 1, 2, 3
    val icon: Drawable?
)

/**
 * مدل دسته‌بندی برنامه‌های کلون‌شده
 */
data class ClonedAppGroup(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val instances: List<ClonedInstance> = emptyList()
)
