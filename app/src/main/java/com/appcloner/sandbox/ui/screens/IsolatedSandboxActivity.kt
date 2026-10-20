package com.appcloner.sandbox.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcloner.sandbox.core.SystemDualAppHelper
import com.appcloner.sandbox.ui.theme.AppClonerSandboxTheme
import com.appcloner.sandbox.ui.theme.DarkBackground
import com.appcloner.sandbox.ui.theme.PrimaryIndigo
import com.appcloner.sandbox.ui.theme.TextPrimary
import com.appcloner.sandbox.ui.theme.TextSecondary
import com.appcloner.sandbox.ui.theme.AccentCyan

class IsolatedSandboxActivity : ComponentActivity() {

    companion object {
        fun start(context: Context, packageName: String, appName: String, userId: Int) {
            val intent = Intent(context, IsolatedSandboxActivity::class.java).apply {
                putExtra("EXTRA_PACKAGE_NAME", packageName)
                putExtra("EXTRA_APP_NAME", appName)
                putExtra("EXTRA_USER_ID", userId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private var targetUrl: String? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra("EXTRA_PACKAGE_NAME") ?: ""
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: "برنامه"
        val userId = intent.getIntExtra("EXTRA_USER_ID", 0)
        val cloneNumber = userId + 1

        targetUrl = when {
            packageName.contains("telegram") || packageName.contains("challegram") -> "https://web.telegram.org/a/"
            packageName.contains("whatsapp") -> "https://web.whatsapp.com"
            packageName.contains("instagram") -> "https://www.instagram.com/accounts/login/"
            packageName.contains("twitter") || packageName.contains("x.android") -> "https://mobile.twitter.com/login"
            packageName.contains("eitaa") -> "https://web.eitaa.com"
            packageName.contains("bale") -> "https://web.bale.ai"
            packageName.contains("rubika") -> "https://web.rubika.ir"
            else -> null
        }

        setContent {
            AppClonerSandboxTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "$appName $cloneNumber (محیط ایزوله)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "نسخه شماره $cloneNumber",
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
                                IconButton(onClick = {
                                    SystemDualAppHelper.openSystemDualAppSetting(this@IsolatedSandboxActivity, packageName)
                                }) {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = "تنظیمات گوشی", tint = TextSecondary)
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
                                url = targetUrl!!,
                                userId = userId,
                                packageName = packageName
                            )
                        } else {
                            GenericAppSandboxView(
                                packageName = packageName,
                                appName = appName,
                                cloneNumber = cloneNumber
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun IsolatedWebView(url: String, userId: Int, packageName: String) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
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
                    settings.userAgentString = settings.userAgentString.replace("wv", "")

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                }
            }
        )
    }

    @Composable
    private fun GenericAppSandboxView(packageName: String, appName: String, cloneNumber: Int) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$cloneNumber",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "فعال‌سازی نسخه خام $appName $cloneNumber",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "برای اجرای نسخه مستقل بدون دستکاری نرم‌افزار اصلی، می‌توانید آن را در بخش کلونر رسمی گوشی فعال نمایید:",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    SystemDualAppHelper.openSystemDualAppSetting(this@IsolatedSandboxActivity, packageName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("فعال‌سازی در شبیه‌ساز رسمی گوشی (Dual App)")
            }
        }
    }
}
