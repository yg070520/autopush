package top.jatus.ibkr;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ForegroundAppAccessibilityService extends AccessibilityService {

    private static final long TOAST_DURATION_MILLIS = 1_000L;
    private static final long DUPLICATE_EVENT_WINDOW_MILLIS = 300L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String lastPackageName;
    private long lastEventTimeMillis;
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
        long eventTimeMillis = System.currentTimeMillis();
        if (packageName.equals(lastPackageName)
            && eventTimeMillis - lastEventTimeMillis < DUPLICATE_EVENT_WINDOW_MILLIS) {
            return;
        }

        lastPackageName = packageName;
        lastEventTimeMillis = eventTimeMillis;
        showPackageName(packageName);
    }

    @Override
    public void onInterrupt() {
        dismissToast.run();
    }

    private void showPackageName(String packageName) {
        mainHandler.removeCallbacks(dismissToast);
        if (appNameToast != null) {
            appNameToast.cancel();
        }

        appNameToast = Toast.makeText(this, packageName, Toast.LENGTH_SHORT);
        appNameToast.show();
        mainHandler.postDelayed(dismissToast, TOAST_DURATION_MILLIS);
    }
}