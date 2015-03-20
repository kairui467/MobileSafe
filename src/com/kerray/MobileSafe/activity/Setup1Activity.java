package com.kerray.MobileSafe.activity;

import android.content.Intent;
import android.os.Bundle;
import com.kerray.MobileSafe.R;

/**
 * @Created by kerray on 2015/3/18.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/18
 */
public class Setup1Activity extends BaseSetupActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup1);
    }

    @Override
    public void showNext()
    {
        Intent intent = new Intent(this, Setup2Activity.class);
        startActivity(intent);
        finish();
        //要求在finish()或者startActivity(intent);后面执行；
        overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
    }

    @Override
    public void showPre()
    {

    }
}