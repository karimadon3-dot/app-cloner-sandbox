package com.appcloner.sandbox.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.appcloner.sandbox.core.PermissionHelper
import com.appcloner.sandbox.ui.theme.*

@Composable
fun PermissionCheckBanner(
    onDismissBanner: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showPermissionDialog by remember { mutableStateOf(false) }

    val hasStorage = PermissionHelper.hasStoragePermission(context)
    val hasOverlay = PermissionHelper.hasOverlayPermission(context)
    val hasBattery = PermissionHelper.isIgnoringBatteryOptimizations(context)

    val allGranted = hasStorage && hasOverlay && hasBattery

    if (!allGranted) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, PrimaryIndigo.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { showPermissionDialog = true },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تنظیم دسترسی‌های سیستمی کلونر",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "جهت اجرای بدون محدودیت برنامه‌ها کلیک کنید",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }
    }

    if (showPermissionDialog && activity != null) {
        Dialog(onDismissRequest = { showPermissionDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "دسترسی‌های مورد نیاز برنامه",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showPermissionDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionItemRow(
                        title = "دسترسی مدیریت فایل‌ها",
                        desc = "جهت ایجاد پوشه اختصاصی داده‌ها برای هر نسخه",
                        isGranted = hasStorage,
                        onRequest = { PermissionHelper.requestStoragePermission(activity) }
                    )

                    PermissionItemRow(
                        title = "دسترسی نمایش روی سایر برنامه‌ها",
                        desc = "جهت باز شدن سریع و روان پنجره کلون‌ها",
                        isGranted = hasOverlay,
                        onRequest = { PermissionHelper.requestOverlayPermission(activity) }
                    )

                    PermissionItemRow(
                        title = "عدم محدودیت مصرف باتری",
                        desc = "جهت فعال ماندن نسخه‌های پشتیبان در پس‌زمینه",
                        isGranted = hasBattery,
                        onRequest = { PermissionHelper.requestIgnoreBatteryOptimizations(activity) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showPermissionDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تأیید و بازگشت")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    desc: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceLight)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = TextSecondary, fontSize = 10.5.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "فعال", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "اجازه دادن", fontSize = 11.sp)
            }
        }
    }
}
