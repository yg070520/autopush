package top.jatus.ibkr;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化极光推送（debug 模式打开日志，正式发布改为 false）
        JPushInterface.setDebugMode(BuildConfig.DEBUG);
        JPushInterface.init(this);
    }
}
