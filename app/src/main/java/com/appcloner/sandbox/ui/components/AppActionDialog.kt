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
                // هدر
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
                            text = "شناسه کانتینر: ${item.userId}",
                            color = AccentCyan,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // گزینه‌های منو
                ActionRowItem(
                    icon = Icons.Default.PlayArrow,
                    label = "اجرای ${item.displayName}",
                    tint = NeonGreen,
                    onClick = {
                        onDismiss()
                        onLaunch()
                    }
                )

                ActionRowItem(
                    icon = Icons.Default.PushPin,
                    label = "افزودن میانبر به صفحه اصلی گوشی",
                    tint = AccentCyan,
                    onClick = {
                        onDismiss()
                        onPinToHomeScreen()
                    }
                )

                ActionRowItem(
                    icon = Icons.Default.Add,
                    label = "ساخت نسخه بعدی از این برنامه",
                    tint = PrimaryIndigo,
                    onClick = {
                        onDismiss()
                        onCloneNextInstance()
                    }
                )

                ActionRowItem(
                    icon = Icons.Default.Delete,
                    label = "حذف این نسخه",
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
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
