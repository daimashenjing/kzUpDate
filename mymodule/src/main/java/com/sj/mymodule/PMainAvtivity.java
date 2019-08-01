package com.sj.mymodule;

import android.app.Activity;
import android.os.Bundle;

public class PMainAvtivity extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l_push);
        BaseModuleUtil.startActivity(this, null);
    }
}
