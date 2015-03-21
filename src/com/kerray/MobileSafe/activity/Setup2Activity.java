package com.kerray.MobileSafe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.ui.SettingItemView;

/**
 * @Created by kerray on 2015/3/18.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/18
 */
public class Setup2Activity extends BaseSetupActivity
{
    private SettingItemView siv_setup2_sim;
    private TelephonyManager tm;                                //读取手机sim的信息

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);

        siv_setup2_sim = (SettingItemView) findViewById(R.id.siv_setup2_sim);
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        String sim = sp.getString("sim", null);

        if (TextUtils.isEmpty(sim))
            siv_setup2_sim.setChecked(false);                   //没有绑定
        else
            siv_setup2_sim.setChecked(true);                    //已经绑定

        siv_setup2_sim.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = sp.edit();

                if (siv_setup2_sim.isChecked())
                {
                    siv_setup2_sim.setChecked(false);
                    //保存sim卡的序列号
                    editor.putString("sim", null);
                } else
                {
                    siv_setup2_sim.setChecked(true);
                    //保存sim卡的序列号
                    String sim = tm.getSimSerialNumber();
                    Log.i("kerray", "sim=====" + sim);
                    editor.putString("sim", sim);
                }
                editor.commit();
            }
        });
    }

    @Override
    public void showNext()
    {
        //取出是否绑定sim
        String sim = sp.getString("sim", null);
        if (TextUtils.isEmpty(sim))
        {
            //没有绑定
            Toast.makeText(getApplicationContext(), "sim卡没有绑定", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
    }

    @Override
    public void showPre()
    {
        Intent intent = new Intent(this, Setup1Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
    }
}