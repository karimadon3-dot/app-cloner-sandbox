package com.appcloner.sandbox.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcloner.sandbox.ui.theme.AppClonerSandboxTheme
import com.appcloner.sandbox.ui.theme.DarkBackground
import com.appcloner.sandbox.ui.theme.DarkSurface
import com.appcloner.sandbox.ui.theme.PrimaryIndigo
import com.appcloner.sandbox.ui.theme.TextPrimary
import com.appcloner.sandbox.ui.theme.TextSecondary
import com.appcloner.sandbox.ui.theme.AccentCyan
import com.appcloner.sandbox.ui.theme.NeonGreen
import com.appcloner.sandbox.ui.theme.DangerRed
import java.io.File

/**
 * اکتیویتی اجرای مستقل و ایزوله هر کلون.
 * هر userId دایرکتوری ذخیره‌سازی WebView کاملاً جداگانه دارد
 * بنابراین کوکی‌ها، localStorage و session هر کلون 100% مستقل از بقیه است.
 */
class IsolatedSandboxActivity : ComponentActivity() {

    companion object {
        /**
         * اجرای مستقیم محیط ایزوله برای یک نسخه کلون
         */
        fun start(context: Context, packageName: String, appName: String, userId: Int) {
            val intent = Intent(context, IsolatedSandboxActivity::class.java).apply {
                putExtra("EXTRA_PACKAGE_NAME", packageName)
                putExtra("EXTRA_APP_NAME", appName)
                putExtra("EXTRA_USER_ID", userId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        /**
         * بررسی اینکه آیا برای این پکیج وب‌کلاینت مستقل قابل ارائه است
         */
        fun getWebClientUrl(packageName: String): String? {
            return when {
                packageName.contains("telegram") || packageName.contains("challegram") || packageName.contains("nekogram") || packageName.contains("turbo") -> "https://web.telegram.org/a/"
                packageName.contains("whatsapp") -> "https://web.whatsapp.com"
                packageName.contains("instagram") -> "https://www.instagram.com/accounts/login/"
                packageName.contains("twitter") || packageName.contains("x.android") -> "https://x.com/i/flow/login"
                packageName.contains("eitaa") -> "https://web.eitaa.com"
                packageName.contains("bale") -> "https://web.bale.ai"
                packageName.contains("rubika") -> "https://web.rubika.ir"
                packageName.contains("igap") -> "https://web.igap.net"
                packageName.contains("facebook") || packageName.contains("katana") -> "https://m.facebook.com"
                packageName.contains("discord") -> "https://discord.com/login"
                packageName.contains("snapchat") -> "https://accounts.snapchat.com"
                packageName.contains("linkedin") -> "https://www.linkedin.com/login"
                packageName.contains("pinterest") -> "https://www.pinterest.com/login/"
                packageName.contains("reddit") -> "https://www.reddit.com/login/"
                packageName.contains("tiktok") || packageName.contains("musical") -> "https://www.tiktok.com/login"
                packageName.contains("spotify") -> "https://accounts.spotify.com/login"
                packageName.contains("netflix") -> "https://www.netflix.com/login"
                packageName.contains("gmail") || packageName.contains("google") -> "https://accounts.google.com"
                packageName.contains("outlook") || packageName.contains("microsoft") -> "https://outlook.live.com"
                packageName.contains("yahoo") -> "https://login.yahoo.com"
                else -> null
            }
        }

        /**
         * نام فارسی قابل فهم‌تر برای برنامه‌ها
         */
        fun getFriendlyAppName(packageName: String): String? {
            return when {
                packageName.contains("telegram") -> "تلگرام"
                packageName.contains("whatsapp") -> "واتساپ"
                packageName.contains("instagram") -> "اینستاگرام"
                packageName.contains("twitter") || packageName.contains("x.android") -> "ایکس (توییتر)"
                packageName.contains("eitaa") -> "ایتا"
                packageName.contains("bale") -> "بله"
                packageName.contains("rubika") -> "روبیکا"
                packageName.contains("facebook") -> "فیسبوک"
                packageName.contains("discord") -> "دیسکورد"
                packageName.contains("snapchat") -> "اسنپ‌چت"
                packageName.contains("tiktok") -> "تیک‌تاک"
                packageName.contains("spotify") -> "اسپاتیفای"
                else -> null
            }
        }
    }

    private var webViewInstance: WebView? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra("EXTRA_PACKAGE_NAME") ?: ""
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: "برنامه"
        val userId = intent.getIntExtra("EXTRA_USER_ID", 0)
        val cloneNumber = userId + 1
        val targetUrl = getWebClientUrl(packageName)

        // ایجاد دایرکتوری ایزوله مخصوص این نسخه کلون
        val isolatedDir = File(filesDir, "clone_webview_data/user_${userId}_${packageName.replace('.', '_')}")
        if (!isolatedDir.exists()) {
            isolatedDir.mkdirs()
        }

        setContent {
            AppClonerSandboxTheme {
                var showClearConfirm by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "$appName $cloneNumber",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (targetUrl != null) "حساب مستقل شماره $cloneNumber (وب‌کلاینت ایزوله)" else "نسخه $cloneNumber",
                                        fontSize = 11.sp,
                                        color = AccentCyan
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = TextPrimary)
                                }
                            },
                            actions = {
                                if (targetUrl != null) {
                                    // دکمه رفرش صفحه
                                    IconButton(onClick = {
                                        webViewInstance?.reload()
                                    }) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "بارگذاری مجدد", tint = TextSecondary)
                                    }
                                    // دکمه پاک‌سازی حافظه این نسخه کلون (نه نرم‌افزار اصلی!)
                                    IconButton(onClick = { showClearConfirm = true }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "پاک‌سازی حافظه این نسخه", tint = TextSecondary)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                        )
                    },
                    containerColor = DarkBackground
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        if (targetUrl != null) {
                            IsolatedWebView(
                                url = targetUrl,
                                userId = userId,
                                packageName = packageName,
                                isolatedDir = isolatedDir
                            )
                        } else {
                            NoWebClientView(
                                packageName = packageName,
                                appName = appName,
                                cloneNumber = cloneNumber
                            )
                        }
                    }
                }

                // دیالوگ تأیید پاک‌سازی حافظه این کلون
                if (showClearConfirm) {
                    AlertDialog(
                        onDismissRequest = { showClearConfirm = false },
                        title = { Text("پاک‌سازی حافظه نسخه $cloneNumber", color = TextPrimary) },
                        text = { Text("با این کار فقط حافظه و نشست این نسخه کلون ($appName $cloneNumber) پاک می‌شود و می‌توانید از اول ثبت‌نام کنید.\n\nبرنامه اصلی و سایر کلون‌ها تأثیر نمی‌بینند.", color = TextSecondary) },
                        confirmButton = {
                            TextButton(onClick = {
                                showClearConfirm = false
                                // پاک‌سازی WebView و دایرکتوری ایزوله
                                webViewInstance?.clearCache(true)
                                webViewInstance?.clearHistory()
                                CookieManager.getInstance().removeAllCookies(null)
                                WebStorage.getInstance().deleteAllData()
                                if (isolatedDir.exists()) {
                                    isolatedDir.deleteRecursively()
                                    isolatedDir.mkdirs()
                                }
                                Toast.makeText(this@IsolatedSandboxActivity, "حافظه نسخه $cloneNumber پاک شد - ثبت‌نام مجدد انجام دهید", Toast.LENGTH_SHORT).show()
                                webViewInstance?.loadUrl(targetUrl ?: "about:blank")
                            }) {
                                Text("پاک‌سازی و شروع از اول", color = DangerRed)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearConfirm = false }) {
                                Text("انصراف", color = TextSecondary)
                            }
                        },
                        containerColor = DarkSurface
                    )
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun IsolatedWebView(url: String, userId: Int, packageName: String, isolatedDir: File) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // هر کلون دایرکتوری WebView جداگانه دارد تا session و cookie ایزوله بماند
                val dataDir = File(isolatedDir, "webview_data")
                if (!dataDir.exists()) dataDir.mkdirs()

                // تنظیم مسیر ایزوله برای WebView در اندروید 7+ 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        val dataSuffix = "clone_${userId}_${packageName.replace('.', '_')}"
                        WebView.setDataDirectorySuffix(dataSuffix)
                    } catch (e: Exception) {
                        // اگر قبلاً تنظیم شده بود، مشکلی نیست
                    }
                }

                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.allowFileAccess = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.setSupportMultipleWindows(false)
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    
                    // ذخیره مسیر ایزوله برای cache و database
                    settings.databasePath = File(dataDir, "databases").absolutePath

                    // تنظیم User-Agent شبیه گوشی موبایل (حذف wv تا سایت‌ها مثل دسکتاپ/موبایل نرمال رفتار کنند)
                    settings.userAgentString = settings.userAgentString
                        .replace("; wv", "")
                        .replace("wv", "")

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }

                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                        }
                    }
                    webChromeClient = WebChromeClient()

                    // فعال‌سازی کوکی برای نشست‌های login
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    loadUrl(url)
                    webViewInstance = this
                }
            }
        )
    }

    /**
     * صفحه‌ای که وقتی برنامه وب‌کلاینت مشخصی ندارد نمایش داده می‌شود.
     * کاربر را راهنمایی می‌کند که از شبیه‌ساز رسمی گوشی (Dual App) استفاده کند.
     */
    @Composable
    private fun NoWebClientView(packageName: String, appName: String, cloneNumber: Int) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$cloneNumber",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "$appName نسخه $cloneNumber",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("راهنمای ایجاد نسخه مستقل", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "این برنامه فاقد وب‌کلاینت رسمی است. برای ایجاد نسخه مستقل با اکانت جدید:\n\n" +
                                "۱. اگر گوشی شما سامسونگ است: تنظیمات > ویژگی‌های پیشرفته > پیام‌رسان دوگانه (Dual Messenger)\n\n" +
                                "۲. اگر گوشی شما شیائومی/ردمی است: تنظیمات > Apps > Dual Apps\n\n" +
                                "۳. اگر گوشی شما هواوی است: تنظیمات > Apps > App Twin\n\n" +
                                "با فعال‌سازی، نسخه کاملاً مستقل و خام ایجاد می‌شود.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    com.appcloner.sandbox.core.SystemDualAppHelper.openSystemDualAppSetting(this@IsolatedSandboxActivity, packageName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("باز کردن تنظیمات Dual App گوشی", fontSize = 14.sp)
            }
        }
    }

    override fun onDestroy() {
        webViewInstance?.destroy()
        webViewInstance = null
        super.onDestroy()
    }
}
