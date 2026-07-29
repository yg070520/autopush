# 极光推送混淆规则
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.service.JPushMessageReceiver { *; }
-keep class cn.jiguang.** { *; }

# WebView JS 接口（如有）
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# 保留 BuildConfig
-keep class top.jatus.ibkr.BuildConfig { *; }
