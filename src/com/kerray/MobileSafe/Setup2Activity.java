package com.kerray.MobileSafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * @Created by kerray on 2015/3/18.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/18
 */
public class Setup2Activity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
    }


    public void next(View view) {
        Intent intent = new Intent(this,Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_in, R.anim.tran_out);

    }

    public void pre(View view) {
        Intent intent = new Intent(this,Setup1Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
    }
}