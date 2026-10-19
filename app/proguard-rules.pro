# Keep virtualization reflection and binder hooks intact
-keep class top.niunaijun.blackbox.** { *; }
-keep class com.appcloner.sandbox.** { *; }
-dontwarn top.niunaijun.blackbox.**
