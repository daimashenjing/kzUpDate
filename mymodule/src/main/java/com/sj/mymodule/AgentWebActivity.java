package com.sj.mymodule;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.download.library.DownloadListenerAdapter;
import com.download.library.Extra;
import com.just.agentweb.AbsAgentWebSettings;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.IAgentWebSettings;
import com.just.agentweb.LogUtils;
import com.just.agentweb.MiddlewareWebChromeBase;
import com.just.agentweb.MiddlewareWebClientBase;
import com.just.agentweb.PermissionInterceptor;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebListenerManager;
import com.just.agentweb.WebViewClient;
import com.just.agentweb.download.DefaultDownloadImpl;
import com.just.agentweb.download.DownloadListener;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateDialogFragment;
import com.vector.update_app.service.DownloadService;
import com.vector.update_app.utils.AppUpdateUtils;

import java.io.File;
import java.util.HashMap;

import rx.Observer;

import static android.view.KeyEvent.KEYCODE_BACK;

public class AgentWebActivity extends Activity implements View.OnClickListener {

    private AgentWeb mAgentWeb;
    private LinearLayout container;
    private LinearLayout layout_goback, layout_forwarck, layout_reload;
    public static String URL = "URL";
    public static String UPDATEURL = "updateUrl";
    public static String MODLETYPE = "modletype";
    public static String IMAGEURL = "image";
    public static String SCREEN = "screen";
    private String url;
    private String updateUrl;
    private String imageUrl;
    private int screenType = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }

        setContentView(R.layout.activity_agent_web);
        url = getIntent().getStringExtra(URL);
        updateUrl = getIntent().getStringExtra(UPDATEURL);
        imageUrl = getIntent().getStringExtra(IMAGEURL);
        screenType = getIntent().getIntExtra(SCREEN, 3);
        container = (LinearLayout) findViewById(R.id.container);
        layout_goback = (LinearLayout) findViewById(R.id.layout_goback);
        layout_forwarck = (LinearLayout) findViewById(R.id.layout_forwork);
        layout_reload = (LinearLayout) findViewById(R.id.layout_reload);
        layout_goback.setOnClickListener(this);
        layout_forwarck.setOnClickListener(this);
        layout_reload.setOnClickListener(this);
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
                .setWebViewClient(mWebViewClient)
                .setPermissionInterceptor(mPermissionInterceptor)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
                .createAgentWeb()
                .ready()
                .go(url);
        this.getRxPermissions();

    }


    protected PermissionInterceptor mPermissionInterceptor = new PermissionInterceptor() {

        @Override
        public boolean intercept(String url, String[] permissions, String action) {
            return false;
        }
    };

    /**
     * 获取权限
     */
    private void getRxPermissions() {
        //动态申请内存存储权限
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            if (!TextUtils.isEmpty(updateUrl)) {
                                update(updateUrl);
                            }
                        } else {
                            Toast.makeText(AgentWebActivity.this, "请开启权限", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void update(final String url) {
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

    private Activity getActivity() {
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
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            Runtime.getRuntime().exit(0);
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }
}
