package top.jatus.ibkr;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);  // 调试期间开启，上线后可改为 false
        JPushInterface.init(this);
    }
}
