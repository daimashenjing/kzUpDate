package com.sj.mymodule;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.nostra13.universalimageloader.utils.L;


import org.json.JSONObject;

import cn.bmob.push.PushConstants;
import cn.bmob.v3.util.BmobNotificationManager;

public class PushMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("msg");
        L.d("dasdaasdasd",msg+":::::");
        if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {

            Intent pendingIntent = new Intent(context, PushLAvtivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            BmobNotificationManager.getInstance(context).showNotification(largeIcon, getTitle(msg),
                    getContext(msg), msg, pendingIntent,
                    NotificationManager.IMPORTANCE_MIN, NotificationCompat.FLAG_ONLY_ALERT_ONCE);
        }
    }

    public String  getContext(String msg){
        try {
            JSONObject jsonObject = new JSONObject(msg);
            return jsonObject.get("alert").toString();
        }catch (Exception e){
            e.printStackTrace();
            try {
                JSONObject jsonObject = new JSONObject(msg);
                return jsonObject.get("msg").toString();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        }
        return "新消息，请查看";
    }
    public String getTitle(String msg){
        try {
            JSONObject jsonObject = new JSONObject(msg);
            return jsonObject.get("title").toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "有新的消息";
    }
}
