package com.appcloner.sandbox.data.model

import android.graphics.drawable.Drawable

/**
 * مدل نمایانگر هر برنامه نصب شده در سیستم اندروید
 */
data class InstalledApp(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false,
    val versionName: String = ""
)

/**
 * مدل نمایانگر یک نسخه مستقل (کلون) با User ID مجزا داخل سندباکس
 */
data class ClonedInstance(
    val userId: Int,
    val instanceLabel: String,
    val isRunning: Boolean = false
)

/**
 * مدل نمایانگر برنامه‌ای که یک یا چند کلون از آن ساخته شده است
 */
data class ClonedAppGroup(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val instances: List<ClonedInstance> = emptyList()
)
