package top.jatus.ibkr;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

public class ForegroundAppAccessibilityService extends AccessibilityService {

    private static final String AUTOMATION_PREFERENCES = "clash_automation";
    private static final String AUTOMATION_ENABLED_KEY = "enabled";
    private static final long DUPLICATE_EVENT_WINDOW_MILLIS = 300L;
    private static final long LAUNCHER_STOP_DELAY_MILLIS = 2_000L;
    private static final String CLASH_PACKAGE = "com.github.metacubex.clash.meta";
    private static final String CLASH_CONTROL_ACTIVITY =
        "com.github.kr328.clash.ExternalControlActivity";
    private static final String CLASH_ACTION_START =
        "com.github.metacubex.clash.meta.action.START_CLASH";
    private static final String CLASH_ACTION_STOP =
        "com.github.metacubex.clash.meta.action.STOP_CLASH";

    private String lastPackageName;
    private long lastEventTimeMillis;
    private boolean clashRequestedRunning;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable stopClashWhenStillOnLauncher = () -> {
        if (clashRequestedRunning && isLauncherPackage(lastPackageName)) {
            controlClash(CLASH_ACTION_STOP);
            clashRequestedRunning = false;
        }
    };
    private ClashAppListStore appListStore;
    private WindowManager windowManager;
    private TextView packageNameView;
    private SharedPreferences automationPreferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener automationPreferenceListener =
            (preferences, key) -> {
                if (AUTOMATION_ENABLED_KEY.equals(key) && !isAutomationEnabled(this)) {
                    mainHandler.removeCallbacks(stopClashWhenStillOnLauncher);
                    removePackageNameOverlay();
                }
            };

    public static boolean isAutomationEnabled(android.content.Context context) {
        return context.getSharedPreferences(AUTOMATION_PREFERENCES, MODE_PRIVATE)
                .getBoolean(AUTOMATION_ENABLED_KEY, true);
    }

    public static void setAutomationEnabled(android.content.Context context, boolean enabled) {
        context.getSharedPreferences(AUTOMATION_PREFERENCES, MODE_PRIVATE)
                .edit()
                .putBoolean(AUTOMATION_ENABLED_KEY, enabled)
                .apply();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        appListStore = new ClashAppListStore(this);
        automationPreferences = getSharedPreferences(AUTOMATION_PREFERENCES, MODE_PRIVATE);
        automationPreferences.registerOnSharedPreferenceChangeListener(automationPreferenceListener);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAutomationEnabled(this)) {
            return;
        }

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
        updateClashState(packageName);
    }

    @Override
    public void onInterrupt() {
        mainHandler.removeCallbacks(stopClashWhenStillOnLauncher);
        removePackageNameOverlay();
    }

    private void showPackageName(String packageName) {
        if (packageNameView == null) {
            windowManager = getSystemService(WindowManager.class);
            packageNameView = createPackageNameView();
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    -3);
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            layoutParams.y = dpToPixels(96);
            windowManager.addView(packageNameView, layoutParams);
        }

        packageNameView.setText(packageName);
    }

    @Override
    public void onDestroy() {
        mainHandler.removeCallbacks(stopClashWhenStillOnLauncher);
        removePackageNameOverlay();
        if (automationPreferences != null) {
            automationPreferences.unregisterOnSharedPreferenceChangeListener(automationPreferenceListener);
        }
        if (appListStore != null) {
            appListStore.close();
        }
        super.onDestroy();
    }

    private TextView createPackageNameView() {
        TextView textView = new TextView(this);
        int horizontalPadding = dpToPixels(12);
        int verticalPadding = dpToPixels(8);
        textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(14);

        GradientDrawable background = new GradientDrawable();
        background.setColor(0xCC202124);
        background.setCornerRadius(dpToPixels(6));
        textView.setBackground(background);
        return textView;
    }

    private void removePackageNameOverlay() {
        if (packageNameView != null && windowManager != null) {
            windowManager.removeView(packageNameView);
        }
        packageNameView = null;
        windowManager = null;
    }

    private void updateClashState(String packageName) {
        if (isClashApp(packageName) && !clashRequestedRunning) {
            mainHandler.removeCallbacks(stopClashWhenStillOnLauncher);
            controlClash(CLASH_ACTION_START);
            clashRequestedRunning = true;
            return;
        }

        if (!isLauncherPackage(packageName)) {
            mainHandler.removeCallbacks(stopClashWhenStillOnLauncher);
            return;
        }

        if (clashRequestedRunning) {
            mainHandler.removeCallbacks(stopClashWhenStillOnLauncher);
            mainHandler.postDelayed(stopClashWhenStillOnLauncher, LAUNCHER_STOP_DELAY_MILLIS);
        }
    }

    private boolean isClashApp(String packageName) {
        return appListStore != null && appListStore.contains(packageName);
    }

    private boolean isLauncherPackage(String packageName) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setPackage(packageName);
        return getPackageManager().resolveActivity(homeIntent, 0) != null;
    }

    private void controlClash(String action) {
        Intent intent = new Intent(action);
        intent.setComponent(new ComponentName(CLASH_PACKAGE, CLASH_CONTROL_ACTIVITY));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (RuntimeException ignored) {
            // Clash Meta may not be installed or may reject the external control intent.
        }
    }

    private int dpToPixels(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}