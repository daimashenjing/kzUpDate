package com.sj.mymodule;

import android.app.Activity;
import android.os.Bundle;

public class PushLAvtivity extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l_push);
        BaseModuleUtil.startActivity(this, null);
    }
}
