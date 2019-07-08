package com.sj.mymodule;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownDialog extends Dialog {

    private ImageView image_banner;
    private TextView tv_current;
    private ProgressBar progressBar;

    public DownDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉默认的title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.load);
        image_banner = (ImageView) findViewById(R.id.image_banner);
        tv_current = (TextView) findViewById(R.id.tv_current);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
    }

    public void setMax(long total) {
        if (progressBar != null) {
            progressBar.setMax(((int) total) / (1024 * 1024));
        }
    }

    public void setImageUrl(String url) {
        if (image_banner != null) {
            if (!TextUtils.isEmpty(url)) {
                image_banner.setVisibility(View.VISIBLE);
                ImageLoadProxy.displayImageList(url, image_banner, android.R.color.transparent, null, null);
            } else {
                image_banner.setVisibility(View.GONE);
            }
        }
    }

    public void setProgress(int current) {
        if (progressBar != null) {
            tv_current.setText("当前进度:" + current + "%");
            progressBar.setProgress(current);
            if (progressBar.getProgress() >= progressBar.getMax()) {
                dismiss();
            }
        }
    }
}
