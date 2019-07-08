package com.sj.mymodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.List;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

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
    public static void init(Context mContext, String applicationId) {
        context = new SoftReference<>(mContext.getApplicationContext());
        Bmob.initialize(mContext.getApplicationContext(), applicationId);
        ImageLoadProxy.initImageLoader(mContext.getApplicationContext());
        SharedPreferencesUtil.init(mContext.getApplicationContext(), mContext.getPackageName() + "_preference", Context.MODE_MULTI_PROCESS);
       try {
           BmobInstallationManager.getInstance().initialize(new InstallationListener<BmobInstallation>() {
               @Override
               public void done(BmobInstallation bmobInstallation, BmobException e) {
                   if(bmobInstallation!=null){

                   }else if(e!=null){
                       e.printStackTrace();
                   }
               }
           });
           BmobPush.startWork(mContext.getApplicationContext());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    /**
     * 调用的方法
     *
     * @param activity
     * @param impStartLister
     */
    public static void startActivity(final Activity activity, final ImpStartLister impStartLister) {
        BmobQuery<CheckUpdata> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(new FindListener<CheckUpdata>() {
            @Override
            public void done(List<CheckUpdata> categories, BmobException e) {
                CheckUpdata avObject = null;
                if (e != null) {
                    if (e.getErrorCode() == 101) {
                        avObject = new CheckUpdata();
                        avObject.setOpenUp(false);
                        avObject.setOpenUrl(false);
                        avObject.setFullscreen(false);
                        avObject.setImage("");
                        avObject.setUrl("");
                        avObject.setUrlUp("");
                        avObject.setScreen(3);
                        avObject.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (e == null) {
                                    Log.e("aasssaa", "done: " + s);
                                } else {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                    return;
                }
                if (categories != null && categories.size() > 0) {
                    avObject = categories.get(0);
                    if (avObject != null) {
                        int modleType = SharedPreferencesUtil.getInstance().getInt(AgentWebActivity.MODLETYPE, 0);
                        String localUrl = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.URL, "");
                        String localupdateUrl = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.UPDATEURL, "");
                        String localimageUrl = SharedPreferencesUtil.getInstance().getString(AgentWebActivity.IMAGEURL, "");

                        SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.SCREEN, avObject.getScreen());
                        SharedPreferencesUtil.getInstance().putBoolean(AgentWebActivity.FSCREEN, avObject.isFullscreen());

                        if (avObject.isOpenUp() && avObject.isOpenUrl()) {
                            String upteUrl = avObject.getUrlUp();
                            String url = avObject.getUrl();
                            String imageUrl = avObject.getImage();
                            SharedPreferencesUtil.getInstance().putString(AgentWebActivity.URL, url);
                            SharedPreferencesUtil.getInstance().putString(AgentWebActivity.UPDATEURL, upteUrl);
                            SharedPreferencesUtil.getInstance().putString(AgentWebActivity.IMAGEURL, imageUrl);
                            SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.MODLETYPE, 1);
                            startWebViewActivity(activity, url, upteUrl, imageUrl);
                        } else if (avObject.isOpenUp()) {
                            String upteUrl = avObject.getUrlUp();
                            String imageUrl = avObject.getImage();
                            SharedPreferencesUtil.getInstance().putString(AgentWebActivity.IMAGEURL, imageUrl);
                            SharedPreferencesUtil.getInstance().putString(AgentWebActivity.UPDATEURL, upteUrl);
                            SharedPreferencesUtil.getInstance().putInt(AgentWebActivity.MODLETYPE, 2);
                            startWebViewActivity(activity, "", upteUrl, imageUrl);
                        } else if (avObject.isOpenUrl()) {
                            String url = avObject.getUrl();
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
            }
        });
    }

    private static void startWebViewActivity(Activity activity, String url, String updataUrl, String imageUrl) {
        int localscreen = SharedPreferencesUtil.getInstance().getInt(AgentWebActivity.SCREEN, 3);
        boolean isFullscreen = SharedPreferencesUtil.getInstance().getBoolean(AgentWebActivity.FSCREEN, false);
        Intent intent = new Intent(activity, AgentWebActivity.class);
        intent.putExtra(AgentWebActivity.URL, url);
        intent.putExtra(AgentWebActivity.UPDATEURL, updataUrl);
        intent.putExtra(AgentWebActivity.IMAGEURL, imageUrl);
        intent.putExtra(AgentWebActivity.SCREEN, localscreen);
        intent.putExtra(AgentWebActivity.FSCREEN, isFullscreen);
        activity.startActivity(intent);
        activity.finish();
    }

    public interface ImpStartLister {
        void start();
    }
}
