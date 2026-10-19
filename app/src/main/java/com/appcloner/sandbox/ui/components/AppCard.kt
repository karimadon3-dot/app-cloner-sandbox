package com.appcloner.sandbox.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcloner.sandbox.data.model.ClonedAppGroup
import com.appcloner.sandbox.data.model.InstalledApp
import com.appcloner.sandbox.ui.theme.*

/**
 * تبدیل Drawable به ImageBitmap برای نمایش روان در کامپوز
 */
fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun AppIconView(drawable: Drawable?, modifier: Modifier = Modifier.size(48.dp)) {
    val bitmap = drawableToBitmap(drawable)
    if (bitmap != null) {
        Image(
            painter = BitmapPainter(bitmap.asImageBitmap()),
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = PrimaryIndigo
            )
        }
    }
}

/**
 * کارت نمایش برنامه‌های شبیه‌سازی شده
 */
@Composable
fun ClonedAppCard(
    clonedApp: ClonedAppGroup,
    onManageInstances: () -> Unit,
    onQuickLaunch: (userId: Int) -> Unit,
    onAddClone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onManageInstances() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconView(drawable = clonedApp.icon)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clonedApp.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryIndigo.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${clonedApp.instances.size} نسخه فعال",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // دکمه ساخت کلون جدید (+)
            IconButton(
                onClick = onAddClone,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceLight)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن نسخه",
                    tint = AccentCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // دکمه اجرای سریع اولین نسخه
            Button(
                onClick = {
                    val defaultUserId = clonedApp.instances.firstOrNull()?.userId ?: 0
                    onQuickLaunch(defaultUserId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "اجرا", fontSize = 12.sp)
            }
        }
    }
}

/**
 * کارت نمایش لیست تمام برنامه‌های گوشی جهت انتخاب برای کلون کردن
 */
@Composable
fun InstalledAppCard(
    app: InstalledApp,
    isAlreadyCloned: Boolean,
    onCloneClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconView(drawable = app.icon)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onCloneClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlreadyCloned) DarkSurfaceLight else PrimaryIndigo
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isAlreadyCloned) AccentCyan else TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAlreadyCloned) "نسخه دیگر (+)" else "کلون سازی",
                    fontSize = 12.sp,
                    color = if (isAlreadyCloned) AccentCyan else TextPrimary
                )
            }
        }
    }
}
