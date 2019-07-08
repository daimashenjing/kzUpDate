package com.sj.mymodule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.just.agentweb.AbsAgentWebSettings;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.IAgentWebSettings;
import com.just.agentweb.WebListenerManager;
import com.just.agentweb.WebViewClient;
import com.just.agentweb.download.DefaultDownloadImpl;
import com.just.agentweb.download.DownloadListener;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.service.DownloadService;
import com.vector.update_app.utils.AppUpdateUtils;

import java.io.File;

import static android.view.KeyEvent.KEYCODE_BACK;

public class AgentWebActivity extends FragmentActivity implements View.OnClickListener {

    private AgentWeb mAgentWeb;
    private LinearLayout container;
    private LinearLayout layout_goback, layout_forwarck, layout_reload, layout_home;
    public static String URL = "URL";
    public static String UPDATEURL = "updateUrl";
    public static String UPDATEURL2 = "updateUrl2";
    public static String MODLETYPE = "modletype";
    public static String IMAGEURL = "image";
    public static String SCREEN = "screen";
    public static String FSCREEN = "f_screen";
    public static String APKPACKAGENAME = "ApkPackageName";
    private final static int REQUEST_CODE = 13210;
    private String url;
    private String updateUrl;
    private String imageUrl;
    private int screenType = 3;
    private boolean isFullscreen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_web);
        Intent mIntent = getIntent();
        if (mIntent != null) {
            url = mIntent.getStringExtra(URL);
            updateUrl = mIntent.getStringExtra(UPDATEURL);
            imageUrl = mIntent.getStringExtra(IMAGEURL);
            screenType = mIntent.getIntExtra(SCREEN, 3);
            isFullscreen = mIntent.getBooleanExtra(FSCREEN, false);
        } else {
            finish();
            return;
        }
        container = findViewById(R.id.container);
        layout_goback = findViewById(R.id.layout_goback);
        layout_forwarck = findViewById(R.id.layout_forwork);
        layout_reload = findViewById(R.id.layout_reload);
        layout_home = findViewById(R.id.layout_home);
        layout_goback.setOnClickListener(this);
        layout_forwarck.setOnClickListener(this);
        layout_reload.setOnClickListener(this);
        layout_home.setOnClickListener(this);
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
                .setWebViewClient(mWebViewClient)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
                .createAgentWeb()
                .ready()
                .go(url);
        this.getRxPermissions();
        initPushService();
        if (isFullscreen) {
            findViewById(R.id.tabBar).setVisibility(View.GONE);
        } else {
            findViewById(R.id.tabBar).setVisibility(View.VISIBLE);
        }
    }

    private static int isInItPush = 1;

    private void initPushService() {
        if (isInItPush == 1) {
            isInItPush = 2;
        }

    }


    /**
     * 获取权限
     */
    @SuppressLint("CheckResult")
    private void getRxPermissions() {
        if (getAndroidSDKVersion() >= 23) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_CODE);
        } else {
            if (!TextUtils.isEmpty(updateUrl)) {
                update(updateUrl);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!TextUtils.isEmpty(updateUrl)) {
                    update(updateUrl);
                }
            } else {
                Toast.makeText(AgentWebActivity.this, "请开启权限", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void update(final String url) {
        try {
            String mPackageName = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.APKPACKAGENAME);
            String url2 = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.UPDATEURL2);
            if (!TextUtils.isEmpty(mPackageName) && AppUpdateUtils.isApkInstalled(getActivity(), mPackageName) && !TextUtils.isEmpty(url2) && url2.equals("url")) {
                PackageManager packageManager = getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(mPackageName);
                if (intent != null) {
                    startActivity(intent);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.UPDATEURL2, url);
        UpdateAppBean updateAppBean = new UpdateAppBean();
        //设置 apk 的下载地址
        updateAppBean.setApkFileUrl(url);
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
            try {
                path = getExternalCacheDir().getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(path)) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
        } else {
            path = getCacheDir().getAbsolutePath();
        }
        //设置apk 的保存路径
        updateAppBean.setTargetPath(path);
        //实现网络接口，只实现下载就可以
        updateAppBean.setHttpManager(new UpdateAppHttpUtil());
        UpdateAppManager.download(this, updateAppBean, new DownloadService.DownloadCallback() {
            @Override
            public void onStart() {
                DownDialogUtils.showHorizontalProgressDialog(AgentWebActivity.this, imageUrl);
            }

            @Override
            public void onProgress(float progress, long totalSize) {
                DownDialogUtils.setProgress(Math.round(progress * 100));
            }

            @Override
            public void setMax(long totalSize) {
            }

            @Override
            public boolean onFinish(File file) {
                HProgressDialogUtils.cancel();
                return true;
            }

            @Override
            public void onError(String msg) {

                HProgressDialogUtils.cancel();
            }

            @Override
            public boolean onInstallAppAndAppOnForeground(File file) {
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layout_forwork) {
            mAgentWeb.getWebCreator().getWebView().goForward();
        } else if (v.getId() == R.id.layout_goback) {
            mAgentWeb.getWebCreator().getWebView().goBack();
        } else if (v.getId() == R.id.layout_reload) {
            mAgentWeb.getWebCreator().getWebView().reload();
        } else if (v.getId() == R.id.layout_home) {
            mAgentWeb.getUrlLoader().loadUrl(url);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mAgentWeb.getWebLifeCycle().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (screenType == 1) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else if (screenType == 2) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        mAgentWeb.getWebLifeCycle().onResume();
    }

    private FragmentActivity getActivity() {
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAgentWeb.getWebLifeCycle().onDestroy();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("intent://") && url.contains("com.youku.phone")) {
                return true;
            }
            return false;
        }
    };

    public IAgentWebSettings getSettings() {
        return new AbsAgentWebSettings() {
            private AgentWeb mAgentWeb;

            @Override
            protected void bindAgentWebSupport(AgentWeb agentWeb) {
                this.mAgentWeb = agentWeb;
            }

            @Override
            public WebListenerManager setDownloader(WebView webView, android.webkit.DownloadListener downloadListener) {
                return super.setDownloader(webView, DefaultDownloadImpl.create(getActivity(), webView, mSimpleDownloadListener, mAgentWeb.getPermissionInterceptor()));
            }

            @Override
            public IAgentWebSettings toSetting(WebView webView) {
                IAgentWebSettings agentWebSettings = super.toSetting(webView);
                WebSettings webSettings = agentWebSettings.getWebSettings();
                webSettings.setUseWideViewPort(true);
                return agentWebSettings;
            }
        };
    }


    protected DownloadListener mSimpleDownloadListener = new DownloadListener() {
        @Override
        public boolean onStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, com.just.agentweb.download.Extra extra) {
            extra.setBreakPointDownload(true) // 是否开启断点续传
                    .setConnectTimeOut(6000) // 连接最大时长
                    .setBlockMaxTime(10 * 60 * 1000)  // 以8KB位单位，默认60s ，如果60s内无法从网络流中读满8KB数据，则抛出异常
                    .setDownloadTimeOut(Long.MAX_VALUE) // 下载最大时长
                    .setParallelDownload(true)  // 串行下载更节省资源哦
                    .setEnableIndicator(true)  // false 关闭进度通知
                    .setAutoOpen(true) // 下载完成自动打开
                    .setForceDownload(true); // 强制下载，不管网络网络类型
            return false;
        }

        @Override
        public void onProgress(String url, long loaded, long length, long usedTime) {
            super.onProgress(url, loaded, length, usedTime);
        }

        @Override
        public boolean onResult(Throwable throwable, Uri path, String url, com.just.agentweb.download.Extra extra) {
            if (null == throwable) { //下载成功
                //do you work
            } else {//下载失败

            }
            return false;
        }
    };


    private long mExitTime;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK)) {
            if (mAgentWeb.getWebCreator().getWebView().canGoBack()) {
                mAgentWeb.getWebCreator().getWebView().goBack();
            } else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再次点击退出", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    exit();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        try {
            isInItPush = 1;
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            Runtime.getRuntime().exit(0);
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }

    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }
}
