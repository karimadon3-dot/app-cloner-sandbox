package com.appcloner.sandbox.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.appcloner.sandbox.data.repository.AppRepository
import com.appcloner.sandbox.ui.screens.HomeScreen
import com.appcloner.sandbox.ui.theme.AppClonerSandboxTheme
import com.appcloner.sandbox.ui.theme.DarkBackground

class MainActivity : ComponentActivity() {

    private lateinit var appRepository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appRepository = AppRepository(this)

        handleShortcutLaunch(intent)

        setContent {
            AppClonerSandboxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    HomeScreen(repository = appRepository)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutLaunch(intent)
    }

    private fun handleShortcutLaunch(intent: Intent?) {
        val shortcutPkg = intent?.getStringExtra("EXTRA_PACKAGE_NAME")
        if (shortcutPkg != null) {
            val userId = intent.getIntExtra("EXTRA_USER_ID", 0)
            val (launched, error) = appRepository.launchInstance(this, shortcutPkg, userId)
            if (!launched) {
                Toast.makeText(this, "خطا در اجرای میانبر: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
