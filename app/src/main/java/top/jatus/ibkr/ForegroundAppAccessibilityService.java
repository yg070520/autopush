package top.jatus.ibkr;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ForegroundAppAccessibilityService extends AccessibilityService {

    private static final long TOAST_DURATION_MILLIS = 1_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentPackageName;
    private Toast appNameToast;
    private final Runnable dismissToast = () -> {
        if (appNameToast != null) {
            appNameToast.cancel();
            appNameToast = null;
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getPackageName() == null) {
            return;
        }

        String packageName = event.getPackageName().toString();
        if (packageName.equals(currentPackageName)) {
            return;
        }

        currentPackageName = packageName;
        showAppName(getApplicationLabel(packageName));
    }

    @Override
    public void onInterrupt() {
        dismissToast.run();
    }

    private CharSequence getApplicationLabel(String packageName) {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException exception) {
            return packageName;
        }
    }

    private void showAppName(CharSequence appName) {
        mainHandler.removeCallbacks(dismissToast);
        if (appNameToast != null) {
            appNameToast.cancel();
        }

        appNameToast = Toast.makeText(this, appName, Toast.LENGTH_SHORT);
        appNameToast.show();
        mainHandler.postDelayed(dismissToast, TOAST_DURATION_MILLIS);
    }
}