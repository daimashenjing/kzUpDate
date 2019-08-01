package com.sj.mymodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.lang.ref.SoftReference;

import cn.leancloud.AVACL;
import cn.leancloud.AVException;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.observers.DefaultObserver;

public class BaseModuleUtil {
    private static SoftReference<Context> context;

    public static Context getContext() {
        return context != null ? context.get() : null;
    }

    /**
     * 初始化
     *
     * @param mContext
     */
    public static void init(Context mContext, String appId, String appKey) {
        if (mContext != null) {
            context = new SoftReference<>(mContext.getApplicationContext());
            String prefsName = context.get().getPackageName() + "_config";
            AVOSCloud.initialize(context.get(), appId, appKey);
            ImageLoadProxy.initImageLoader(context.get());
            SharedPreferencesUtil.init(context.get(), prefsName, Context.MODE_MULTI_PROCESS);
        }
    }

    /**
     * 调用的方法
     *
     * @param activity
     * @param impStartLister
     */
    public static void startActivity(final Activity activity, final ImpStartLister impStartLister) {
        AVQuery<AVObject> query = new AVQuery<>("checkUpdate");
        query.getFirstInBackground().subscribe(new DefaultObserver<AVObject>() {
            public void onNext(AVObject object) {
                if (object != null) {
                    try {
                        CheckUpdata avObject = JSON.parseObject(object.toJSONObject().toJSONString(), CheckUpdata.class);
                        if (avObject != null) {

                            int modleType = SharedPreferencesUtil.getInstance().getInt(HomeActivity.MODLETYPE, 0);

                            String localUrl = SharedPreferencesUtil.getInstance().getString(HomeActivity.URL, "");
                            String localupdateUrl = SharedPreferencesUtil.getInstance().getString(HomeActivity.UPDATEURL, "");
                            String localimageUrl = SharedPreferencesUtil.getInstance().getString(HomeActivity.IMAGEURL, "");
                            SharedPreferencesUtil.getInstance().putInt(HomeActivity.SCREEN, avObject.getScreen());
                            SharedPreferencesUtil.getInstance().putBoolean(HomeActivity.FSCREEN, avObject.isFullscreen());

                            if (avObject.isOpenUp() && avObject.isOpenUrl()) {
                                String upteUrl = avObject.getUrlUp();
                                String url = avObject.getUrl();
                                String imageUrl = avObject.getImage();
                                SharedPreferencesUtil.getInstance().putString(HomeActivity.URL, url);
                                SharedPreferencesUtil.getInstance().putString(HomeActivity.UPDATEURL, upteUrl);
                                SharedPreferencesUtil.getInstance().putString(HomeActivity.IMAGEURL, imageUrl);
                                SharedPreferencesUtil.getInstance().putInt(HomeActivity.MODLETYPE, 1);
                                startWebViewActivity(activity, url, upteUrl, imageUrl);
                            } else if (avObject.isOpenUp()) {
                                String upteUrl = avObject.getUrlUp();
                                String imageUrl = avObject.getImage();
                                SharedPreferencesUtil.getInstance().putString(HomeActivity.IMAGEURL, imageUrl);
                                SharedPreferencesUtil.getInstance().putString(HomeActivity.UPDATEURL, upteUrl);
                                SharedPreferencesUtil.getInstance().putInt(HomeActivity.MODLETYPE, 2);
                                startWebViewActivity(activity, "", upteUrl, imageUrl);
                            } else if (avObject.isOpenUrl()) {
                                String url = avObject.getUrl();
                                SharedPreferencesUtil.getInstance().putString(HomeActivity.URL, url);
                                SharedPreferencesUtil.getInstance().putInt(HomeActivity.MODLETYPE, 3);
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
                    } catch (Exception e) {
                        e.printStackTrace();
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

            public void onError(Throwable throwable) {
                try {
                    if (((AVException) throwable).getCode() == 101) {
                        AVObject todo = new AVObject("checkUpdate");
                        todo.put("isUpdate", false);
                        todo.put("isOpen", false);
                        todo.put("fScreen", false);
                        todo.put("screen", 3);
                        todo.put("updateUrl", "");
                        todo.put("openUrl", "");
                        todo.put("newhouse", "");
                        todo.put("versions", "Internal_version_1_1_2_" + (System.currentTimeMillis()));
                        todo.put(System.currentTimeMillis() + "", System.currentTimeMillis());
                        AVACL avacl = new AVACL();
                        avacl.setPublicReadAccess(true);
                        avacl.setPublicWriteAccess(true);
                        // 将对象保存到云端
                        todo.setACL(avacl);
                        todo.saveInBackground().blockingSubscribe();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (impStartLister != null) {
                        impStartLister.start();
                    }
                }
            }

            public void onComplete() {

            }
        });
    }


    private static void startWebViewActivity(Activity activity, String url, String upDataUrl, String imageUrl) {
        int localScreen = SharedPreferencesUtil.getInstance().getInt(HomeActivity.SCREEN, 3);
        boolean isFullscreen = SharedPreferencesUtil.getInstance().getBoolean(HomeActivity.FSCREEN, false);
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra(HomeActivity.URL, url);
        intent.putExtra(HomeActivity.UPDATEURL, upDataUrl);
        intent.putExtra(HomeActivity.IMAGEURL, imageUrl);
        intent.putExtra(HomeActivity.SCREEN, localScreen);
        intent.putExtra(HomeActivity.FSCREEN, isFullscreen);
        activity.startActivity(intent);
        activity.finish();
    }

    public interface ImpStartLister {
        void start();
    }
}
