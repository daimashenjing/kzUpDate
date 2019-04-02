package com.sj.mymodule;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.avos.avoscloud.LogUtil;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.service.DownloadService;

import java.io.File;

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
    private String url;
    private String updateUrl;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_web);
        url = getIntent().getStringExtra(URL);
        updateUrl = getIntent().getStringExtra(UPDATEURL);
        imageUrl = getIntent().getStringExtra(IMAGEURL);
        Log.i("test", "图片" + imageUrl);
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
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setWebChromeClient(webClient)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
                .createAgentWeb()
                .ready()
                .go(url);

        this.getRxPermissions();
    }

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

    WebChromeClient webClient = new WebChromeClient() {

        public void onCloseWindow(Window w) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mAgentWeb.getWebLifeCycle().onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAgentWeb.getWebLifeCycle().onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAgentWeb.getWebLifeCycle().onDestroy();
    }

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
