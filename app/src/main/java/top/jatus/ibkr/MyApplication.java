package top.jatus.ibkr;

import android.app.Application;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        // 打印当前 RegistrationID（已注册过则有值，否则为空）
        String regId = JPushInterface.getRegistrationID(this);
        Log.i("MyApplication", "JPush regId after init: " + regId);
    }
}
