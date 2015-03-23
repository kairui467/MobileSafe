package com.kerray.MobileSafe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.kerray.MobileSafe.R;

/**
 * @Created by kerray on 2015/3/18.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/18
 */
public class Setup4Activity extends BaseSetupActivity
{
    private SharedPreferences sp;

    private CheckBox cb_proteting;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);

        sp = getSharedPreferences("config", MODE_PRIVATE);
        cb_proteting = (CheckBox) findViewById(R.id.cb_proteting);

        boolean protecting = sp.getBoolean("protecting", false);

        if (protecting)
        {
            cb_proteting.setText("手机防盗已经开启");
            cb_proteting.setChecked(true);
        } else
        {
            cb_proteting.setText("手机防盗已经关闭");
            cb_proteting.setChecked(false);
        }

        cb_proteting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                    cb_proteting.setText("手机防盗已经开启");
                else
                    cb_proteting.setText("手机防盗没有开启");

                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("protecting", isChecked);
                editor.commit();
            }
        });


    }

    @Override
    public void showNext()
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("configed", true);
        editor.commit();

        Intent intent = new Intent(this, LostFindActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
    }

    @Override
    public void showPre()
    {
        Intent intent = new Intent(this, Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
    }
}