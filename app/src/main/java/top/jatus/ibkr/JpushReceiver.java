package top.jatus.ibkr;

import android.content.Context;
import android.os.Bundle;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 极光推送消息接收器。
 *
 * 推送通知格式约定（在极光控制台或服务端发送时设置 extras）：
 *   extras: { "url": "https://jatus.top/trades" }
 *
 * 点击通知后，MainActivity.handleNotificationIntent() 会读取 url 并在 WebView 中打开。
 */
public class JpushReceiver extends JPushMessageReceiver {

    /** 收到自定义消息（透传消息，不显示在通知栏） */
    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        super.onMessage(context, customMessage);
        // 如需处理透传消息（例如静默刷新页面数据），在此实现
    }

    /** 收到通知消息 */
    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        // 通知到达，如需本地处理（如更新角标）可在此操作
    }

    /** 用户点击了通知 */
    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        // JPush SDK 会自动拉起 LauncherActivity，
        // MainActivity.handleNotificationIntent() 负责读取 extras 中的 url 并跳转。
    }

    /** 注册成功，获取到 Registration ID */
    @Override
    public void onRegister(Context context, String registrationId) {
        super.onRegister(context, registrationId);
        // 可将 registrationId 上报到你的服务端，用于定向推送
        // 示例：YourApi.uploadRegId(registrationId);
    }
}
