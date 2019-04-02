package com.sj.mymodule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;

/**
 * Created by Vector on 2016/8/12 0012.
 */
public class DownDialogUtils {
    private static DownDialog sHorizontalProgressDialog;

    private DownDialogUtils() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    @SuppressLint("NewApi")
    public static void showHorizontalProgressDialog(Activity context, String url) {
        cancel();
        if (sHorizontalProgressDialog == null) {
            sHorizontalProgressDialog = new DownDialog(context);
            sHorizontalProgressDialog.setCanceledOnTouchOutside(false);
            sHorizontalProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });

        }
        sHorizontalProgressDialog.show();
        sHorizontalProgressDialog.setImageUrl(url);
    }

    public static void setMax(long total) {
        if (sHorizontalProgressDialog != null) {
            sHorizontalProgressDialog.setMax(((int) total) / (1024 * 1024));
        }
    }

    public static void cancel() {
        if (sHorizontalProgressDialog != null) {
            sHorizontalProgressDialog.dismiss();
            sHorizontalProgressDialog = null;
        }
    }

    public static void setProgress(int current) {
        if (sHorizontalProgressDialog == null) {
            return;
        }
        sHorizontalProgressDialog.setProgress(current);
    }
}
