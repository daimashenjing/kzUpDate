package com.sj.mymodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.zhy.http.okhttp.OkHttpUtils;

public class BaseModuleUtil {

    private static String objectId = "";
    private static String applicationId = "";
    private static String clientKey = "";

    /**
     * 初始化
     *
     * @param mContext
     */
    public static void init(Context mContext, String applicationId, String clientKey, String objectId) {
        BaseModuleUtil.applicationId = applicationId;
        BaseModuleUtil.clientKey = clientKey;
        BaseModuleUtil.objectId = objectId;
        OkHttpUtils.getInstance().init(mContext).debug(true, "okHttp").timeout(20 * 1000);
        // 配置 SDK 储存
        AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.API, "https://avoscloud.com");
        // 配置 SDK 云引擎
        AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.ENGINE, "https://avoscloud.com");
        // 配置 SDK 推送
        AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.PUSH, "https://avoscloud.com");
        // 配置 SDK 即时通讯
        AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.RTM, "https://router-g0-push.avoscloud.com");
        AVOSCloud.initialize(mContext, applicationId, clientKey);
        ImageLoadProxy.initImageLoader(mContext);
        SharedPreferencesUtil.init(mContext, mContext.getPackageName() + "_preference", Context.MODE_MULTI_PROCESS);
    }

    /**
     * 调用的方法
     *
     * @param activity
     * @param impStartLister
     */
    public static void startActivity(final Activity activity, final ImpStartLister impStartLister) {
        AVQuery<AVObject> avQuery = new AVQuery<>("switch");
        avQuery.getInBackground(objectId, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (avObject != null) {
                    int modleType = SharedPreferencesUtil.getInstance().getInt(AgentWebActivity.MODLETYPE, 0);
                    String localUrl = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.URL, "");
                    String localupdateUrl = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.UPDATEURL, "");
                    String localimageUrl = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.IMAGEURL, "");

                    if (avObject.containsKey(AgentWebActivity.SCREEN)) {
                        SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.SCREEN, avObject.getInt(AgentWebActivity.SCREEN));
                    }

                    if (avObject.getBoolean("openUp") && avObject.getBoolean("openUrl")) {
                        String upteUrl = avObject.getString("urlUp");
                        String url = avObject.getString("url");
                        String imageUrl = avObject.getString("image");
                        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.URL, url);
                        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.UPDATEURL, upteUrl);
                        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.IMAGEURL, imageUrl);
                        SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.MODLETYPE, 1);
                        startWebViewActivity(activity, url, upteUrl, imageUrl);
                    } else if (avObject.getBoolean("openUp")) {
                        String upteUrl = avObject.getString("urlUp");
                        String imageUrl = avObject.getString("image");
                        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.IMAGEURL, imageUrl);
                        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.UPDATEURL, upteUrl);
                        SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.MODLETYPE, 2);
                        startWebViewActivity(activity, "", upteUrl, imageUrl);
                    } else if (avObject.getBoolean("openUrl")) {
                        String url = avObject.getString("url");
                        SharedPreferencesUtil.getInstance().putString(AgentWebActivity.URL, url);
                        SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.MODLETYPE, 3);
                        startWebViewActivity(activity, url, "", "");
                    } else if (modleType == 1 && !TextUtils.isEmpty(localUrl) && !TextUtils.isEmpty(localupdateUrl)) {
                        startWebViewActivity(activity, localUrl, localupdateUrl, localimageUrl);
                    } else if (modleType == 2 && !TextUtils.isEmpty(localupdateUrl)) {
                        startWebViewActivity(activity, "", localupdateUrl, localimageUrl);
                    } else if (modleType == 3 && !TextUtils.isEmpty(localUrl)) {
                        startWebViewActivity(activity, localUrl, "", "");
                    } else {
                        if (impStartLister != null) {
                            impStartLister.start();
                        }
                    }
                } else {
                    if (impStartLister != null) {
                        impStartLister.start();
                    }
                }
            }
        });
    }

    private static void startWebViewActivity(Activity activity, String url, String updataUrl, String imageUrl) {
        int localscreen = SharedPreferencesUtil.getInstance().getInt(AgentWebActivity.SCREEN, 3);
        Intent intent = new Intent(activity, AgentWebActivity.class);
        intent.putExtra(AgentWebActivity.URL, url);
        intent.putExtra(AgentWebActivity.UPDATEURL, updataUrl);
        intent.putExtra(AgentWebActivity.IMAGEURL, imageUrl);
        intent.putExtra(AgentWebActivity.SCREEN, localscreen);
        activity.startActivity(intent);
        activity.finish();
    }

    public interface ImpStartLister {
        void start();
    }
}
