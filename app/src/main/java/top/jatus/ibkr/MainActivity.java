package top.jatus.ibkr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AppCompatActivity {

    private static final String TARGET_URL = "https://jatus.top";
    private static final int REQUEST_POST_NOTIFICATIONS = 100;

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        setupWebView();
        setupSwipeRefresh();
        requestNotificationPermission();

        // 处理从推送点击跳转过来的 URL
        handleNotificationIntent(getIntent());

        // 首次加载
        webView.loadUrl(TARGET_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // 启用 JavaScript
        settings.setJavaScriptEnabled(true);

        // 本地存储（登录 session 需要）
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // 缓存策略：有网用网络，无网用缓存
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 允许混合内容（HTTPS 页面加载 HTTP 资源，若不需要可去掉）
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        // 支持缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // 自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // User-Agent 追加标识，方便服务端识别 App 请求
        String ua = settings.getUserAgentString();
        settings.setUserAgentString(ua + " JatusIBKRApp/1.0");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // 站内链接在 WebView 内跳转，站外用系统浏览器打开
                if (url.startsWith(TARGET_URL)) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());
    }

    /** 申请 Android 13+ 通知权限 */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 权限结果由 JPush 内部处理，无需额外操作
    }

    /**
     * 处理从推送通知点击后跳转的 Intent。
     * 极光推送在 extras 里传入 "url" 字段，即可跳转到对应页面。
     * 示例推送 extras: {"url": "https://jatus.top/trades"}
     */
    private void handleNotificationIntent(Intent intent) {
        if (intent == null) return;
        Bundle extras = intent.getBundleExtra(cn.jpush.android.api.JPushInterface.EXTRA_NOTIFICATION_EXTRAS);
        if (extras != null) {
            String url = extras.getString("url");
            if (url != null && !url.isEmpty()) {
                webView.loadUrl(url);
                return;
            }
        }
        // 默认不做处理，保持已加载页面
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    /** 返回键：WebView 有历史则后退，否则最小化 App */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // 最小化到后台，不退出进程
        moveTaskToBack(true);
        return true;
    }
}
