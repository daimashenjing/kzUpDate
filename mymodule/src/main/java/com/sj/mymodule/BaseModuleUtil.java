package com.sj.mymodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.zhy.http.okhttp.OkHttpUtils;

public class BaseModuleUtil {

    /**
     * 初实话
     *
     * @param mContext
     */
    public static void init(Context mContext) {
        OkHttpUtils.getInstance().init(mContext).debug(true, "okHttp").timeout(20 * 1000);
        AVOSCloud.initialize(mContext, CommConfig.applicationId, CommConfig.clientKey);
    }

    /**
     * 调用的方法
     *
     * @param activity
     * @param impStartLister
     */
    public static void startActivity(final Activity activity, final ImpStartLister impStartLister) {
        AVQuery<AVObject> avQuery = new AVQuery<>("switch");
        avQuery.getInBackground(CommConfig.objectId, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (avObject != null) {
                    try {
                        if (avObject.getBoolean("openUp") && avObject.getBoolean("openUrl")) {
                            String upteUrl = avObject.getString("urlUp");
                            String url = avObject.getString("url");
                            Intent intent = new Intent(activity, AgentWebActivity.class);
                            intent.putExtra(AgentWebActivity.URL, url);
                            intent.putExtra(AgentWebActivity.UPDATEURL, upteUrl);
                            activity.startActivity(intent);
                            activity.finish();
                        } else if (avObject.getBoolean("openUp")) {
                            String upteUrl = avObject.getString("urlUp");
                            Intent intent = new Intent(activity, AgentWebActivity.class);
                            intent.putExtra(AgentWebActivity.UPDATEURL, upteUrl);
                            activity.startActivity(intent);
                            activity.finish();
                        } else if (avObject.getBoolean("openUrl")) {
                            String url = avObject.getString("url");
                            Intent intent = new Intent(activity, AgentWebActivity.class);
                            intent.putExtra(AgentWebActivity.URL, url);
                            activity.startActivity(intent);
                            activity.finish();
                        } else {
                            if (impStartLister != null) {
                                impStartLister.start();
                            }
                        }
                    } catch (Exception exp) {
                    }
                } else {
                    if (impStartLister != null) {
                        impStartLister.start();
                    }
                }
            }
        });
    }

    //申请权限的方法
//    RxPermissions rxPermissions = new RxPermissions(this);
//        rxPermissions.request(WRITE_EXTERNAL_STORAGE)
//            .subscribe(new Action1<Boolean>() {
//        @Override
//        public void call(Boolean aBoolean) {
//            if (aBoolean) {
//                Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(MainActivity.this, "未授权", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });
    public interface ImpStartLister {
        void start();
    }
}
