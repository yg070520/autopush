package top.jatus.ibkr;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

public class ForegroundAppAccessibilityService extends AccessibilityService {

    private static final long DUPLICATE_EVENT_WINDOW_MILLIS = 300L;

    private String lastPackageName;
    private long lastEventTimeMillis;
    private WindowManager windowManager;
    private TextView packageNameView;

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
            layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            layoutParams.y = dpToPixels(48);
            windowManager.addView(packageNameView, layoutParams);
        }

        packageNameView.setText(packageName);
    }

    @Override
    public void onDestroy() {
        removePackageNameOverlay();
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

    private int dpToPixels(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}