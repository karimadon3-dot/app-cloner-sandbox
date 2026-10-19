package top.niunaijun.blackbox

import android.content.Context
import android.util.Log

class BlackBoxCore private constructor() {
    companion object {
        private const val TAG = "BlackBoxCoreStub"
        private val sInstance = BlackBoxCore()

        @JvmStatic
        fun get(): BlackBoxCore = sInstance
    }

    fun doAttachBaseContext(context: Context, appConfig: Any? = null) {
        Log.i(TAG, "Virtual sandbox core attached to context: ${context.packageName}")
    }

    fun doCreate() {
        Log.i(TAG, "Virtual sandbox core initialized successfully.")
    }

    fun installPackageAsUser(packageName: String, userId: Int): Boolean {
        Log.i(TAG, "Virtual install of package $packageName for userId $userId")
        return true
    }

    fun launchApk(packageName: String, userId: Int): Boolean {
        Log.i(TAG, "Launching virtual instance of $packageName for userId $userId")
        return true
    }

    fun uninstallPackageAsUser(packageName: String, userId: Int): Boolean {
        Log.i(TAG, "Uninstalling virtual instance of $packageName for userId $userId")
        return true
    }
}
