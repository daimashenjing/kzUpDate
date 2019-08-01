package com.sj.mymodule;

import android.app.Activity;
import android.view.KeyEvent;

/**
 * Created by Vector on 2016/8/12 0012.
 */
public class DownDialogUtils {
    private static DownDialog sHorizontalProgressDialog;

    private DownDialogUtils() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void showHorizontalProgressDialog(Activity context, String url) {
        cancel();
        if (sHorizontalProgressDialog == null) {
            sHorizontalProgressDialog = new DownDialog(context);
            sHorizontalProgressDialog.setCanceledOnTouchOutside(false);
            sHorizontalProgressDialog.setOnKeyListener((dialog, keyCode, event) -> {
                return keyCode == KeyEvent.KEYCODE_BACK;
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
