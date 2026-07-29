# 极光推送混淆规则
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.service.JPushMessageReceiver { *; }
-keep class cn.jiguang.** { *; }

# 厂商通道
-dontwarn com.xiaomi.**
-keep class com.xiaomi.** { *; }
-dontwarn com.heytap.**
-keep class com.heytap.** { *; }
-dontwarn com.vivo.**
-keep class com.vivo.** { *; }

# WebView JS 接口
-keepattributes JavascriptInterface
-keepattributes *Annotation*
