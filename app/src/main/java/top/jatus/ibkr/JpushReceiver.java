package top.jatus.ibkr;

import android.content.Context;
import android.util.Log;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class JpushReceiver extends JPushMessageReceiver {

    private static final String TAG = "JpushReceiver";
    private static final String ALIAS = "jatus";

    /** 注册成功，在此设置 alias（确保注册完成后再调用） */
    @Override
    public void onRegister(Context context, String registrationId) {
        super.onRegister(context, registrationId);
        Log.i(TAG, "JPush registered, regId=" + registrationId);
        JPushInterface.setAlias(context, 1, ALIAS);
    }

    /** setAlias 结果回调 */
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage);
        if (jPushMessage.getErrorCode() == 0) {
            Log.i(TAG, "setAlias success: " + jPushMessage.getAlias());
        } else {
            Log.e(TAG, "setAlias failed, errorCode=" + jPushMessage.getErrorCode()
                    + ", retry...");
            // 失败时重试
            JPushInterface.setAlias(context, 2, ALIAS);
        }
    }

    /** 收到通知消息 */
    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage msg) {
        super.onNotifyMessageArrived(context, msg);
        Log.i(TAG, "notification arrived: " + msg.notificationTitle);
    }

    /** 用户点击了通知 */
    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage msg) {
        super.onNotifyMessageOpened(context, msg);
        Log.i(TAG, "notification opened: " + msg.notificationTitle);
    }

    /** 收到透传消息 */
    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        super.onMessage(context, customMessage);
        Log.i(TAG, "custom message: " + customMessage.message);
    }
}
