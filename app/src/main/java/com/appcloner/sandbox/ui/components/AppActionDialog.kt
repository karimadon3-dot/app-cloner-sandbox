package com.appcloner.sandbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.appcloner.sandbox.data.model.HomeScreenAppItem
import com.appcloner.sandbox.ui.theme.*

@Composable
fun AppActionDialog(
    item: HomeScreenAppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onOpenSystemDualApp: () -> Unit,
    onPinToHomeScreen: () -> Unit,
    onCloneNextInstance: () -> Unit,
    onDeleteInstance: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // هدر دیالوگ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIconView(drawable = item.icon, modifier = Modifier.size(46.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.displayName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "شماره نسخه: ${item.instanceNumber}",
                            color = AccentCyan,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = CardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // ۱. اجرای مستقیم این نسخه
                ActionRowItem(
                    icon = Icons.Default.PlayArrow,
                    label = "اجرای این نسخه (${item.displayName})",
                    subtitle = "اجرای مستقیم با یک لمس",
                    tint = NeonGreen,
                    onClick = {
                        onDismiss()
                        onLaunch()
                    }
                )

                // ۲. فعال‌سازی در کلونر رسمی گوشی (Dual App)
                ActionRowItem(
                    icon = Icons.Default.SettingsSuggest,
                    label = "فعال‌سازی در کلونر رسمی گوشی (Dual App)",
                    subtitle = "سامسونگ Dual Messenger / شیائومی Dual Apps (خام و بدون دستکاری برنامه اصلی)",
                    tint = AccentCyan,
                    onClick = {
                        onDismiss()
                        onOpenSystemDualApp()
                    }
                )

                // ۳. افزودن میانبر به صفحه اصلی گوشی
                ActionRowItem(
                    icon = Icons.Default.PushPin,
                    label = "افزودن میانبر به صفحه اصلی گوشی",
                    subtitle = "سنجاق آیکون اختصاصی با شماره روی صفحه اصلی",
                    tint = PurpleGrey80,
                    onClick = {
                        onDismiss()
                        onPinToHomeScreen()
                    }
                )

                // ۴. ساخت شماره بعدی
                ActionRowItem(
                    icon = Icons.Default.Add,
                    label = "ساخت شماره بعدی (نسخه ${item.instanceNumber + 1})",
                    subtitle = "ایجاد کلون جدید در صفحه هوم",
                    tint = PrimaryIndigo,
                    onClick = {
                        onDismiss()
                        onCloneNextInstance()
                    }
                )

                // ۵. حذف
                ActionRowItem(
                    icon = Icons.Default.Delete,
                    label = "حذف این نسخه از صفحه هوم",
                    subtitle = null,
                    tint = DangerRed,
                    onClick = {
                        onDismiss()
                        onDeleteInstance()
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    label: String,
    subtitle: String?,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}
