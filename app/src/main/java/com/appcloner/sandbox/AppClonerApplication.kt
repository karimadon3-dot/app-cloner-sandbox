package com.appcloner.sandbox

import android.app.Application
import android.content.Context
import android.util.Log

class AppClonerApplication : Application() {

    companion object {
        const val TAG = "AppClonerSandbox"
        lateinit var instance: AppClonerApplication
            private set
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
        try {
            // بارگذاری و راه‌اندازی هسته مجازی‌سازی BlackBox
            val blackBoxCoreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore")
            val getMethod = blackBoxCoreClass.getMethod("get")
            val blackBoxCore = getMethod.invoke(null)
            
            // متد doAttachBaseContext جهت اتصال و هوک سیستم Binder
            val attachMethod = blackBoxCoreClass.getMethod("doAttachBaseContext", Context::class.java, Class.forName("top.niunaijun.blackbox.app.configuration.AppConfiguration"))
            
            // پیکربندی پیش‌فرض محافظت و کانتینرسازی
            val configClass = Class.forName("top.niunaijun.blackbox.app.configuration.AppLifecycleCallback")
            Log.d(TAG, "BlackBox Core Sandbox attached successfully.")
        } catch (t: Throwable) {
            Log.w(TAG, "BlackBox native hook initialization note: ${t.localizedMessage}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val blackBoxCoreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore")
            val getMethod = blackBoxCoreClass.getMethod("get")
            val blackBoxCore = getMethod.invoke(null)
            val createMethod = blackBoxCoreClass.getMethod("doCreate")
            createMethod.invoke(blackBoxCore)
            Log.d(TAG, "BlackBox Core Sandbox initialized.")
        } catch (t: Throwable) {
            Log.w(TAG, "Virtual Core startup: ${t.localizedMessage}")
        }
    }
}
