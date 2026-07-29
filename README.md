# Android APK 打包说明

## 目录结构

```
android_app/
├── app/
│   ├── build.gradle              # 依赖配置（JPush、AppKey 在此）
│   ├── proguard-rules.pro        # 混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/top/jatus/ibkr/
│       │   ├── MyApplication.java   # JPush 初始化
│       │   ├── MainActivity.java    # WebView 主界面
│       │   └── JpushReceiver.java   # 推送消息接收器
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/{strings, themes, colors}.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 快速开始

### 1. 填写极光 AppKey

在 `app/build.gradle` 中替换：
```groovy
JPUSH_APPKEY: "YOUR_JPUSH_APPKEY"   // ← 极光控制台 > 应用 > AppKey
```

### 2. 替换应用图标

将图标文件放入：
- `app/src/main/res/mipmap-hdpi/ic_launcher.png`（72×72）
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png`（96×96）
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`（144×144）
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`（192×192）
- 圆角版本同路径，文件名 `ic_launcher_round.png`

### 3. 用 Android Studio 打开

打开 `android_app/` 目录，等待 Gradle Sync 完成。

### 4. 构建 APK

```
Build → Generate Signed Bundle/APK → APK
```

或命令行：
```bash
cd android_app
./gradlew assembleRelease
```
APK 输出路径：`app/build/outputs/apk/release/app-release.apk`

---

## 推送使用说明

### 服务端推送格式

在极光控制台或通过服务端 API 发送推送时，在 **extras** 中添加 `url` 字段，
App 收到通知点击后会自动在 WebView 内打开对应页面：

```json
{
  "notification": {
    "android": {
      "alert": "你有新的交易信号",
      "title": "Jatus IBKR",
      "extras": {
        "url": "https://jatus.top/trades"
      }
    }
  }
}
```

不带 `url` 的推送只展示通知，点击后打开 App 首页。

### Registration ID 上报

`JpushReceiver.onRegister()` 中已留有注释，将 `registrationId` 上报到你的服务端，
即可实现 **定向推送** 到特定用户。

---

## WebView 功能说明

| 功能 | 说明 |
|------|------|
| JavaScript | 已启用，支持所有 Web 功能 |
| DOM Storage | 已启用，登录 Session 持久化 |
| 下拉刷新 | SwipeRefreshLayout，顶部下拉刷新 |
| 进度条 | 页面加载时顶部显示进度 |
| 外链处理 | 站外链接跳转系统浏览器 |
| 返回键 | WebView 历史后退，无历史则最小化 App |
| User-Agent | 追加 `JatusIBKRApp/1.0`，服务端可识别来源 |
