package com.appcloner.sandbox.ui

import android.os.Bundle
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
        appRepository = AppRepository(applicationContext)

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
}
