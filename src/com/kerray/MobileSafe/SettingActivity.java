package com.kerray.MobileSafe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.kerray.MobileSafe.ui.SettingItemView;

/**
 * @Created by kerray on 2015/3/16.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/16
 */
public class SettingActivity extends Activity
{
    private SettingItemView siv_update;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        siv_update = (SettingItemView) findViewById(R.id.siv_update);
        sp = getSharedPreferences("config", MODE_PRIVATE);

        boolean update = sp.getBoolean("update", false);

        if (update)
            siv_update.setChecked(true);                    //自动升级已经开启
        else
            siv_update.setChecked(false);                   //自动升级已经关闭

        siv_update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = sp.edit();
                //判断是否有选中
                //已经打开自动升级了
                if (siv_update.isChecked())
                {
                    siv_update.setChecked(false);
                    editor.putBoolean("update", false);
                } else
                {
                    siv_update.setChecked(true);            //没有打开自动升级
                    editor.putBoolean("update", true);
                }
                editor.commit();
            }
        });
    }
}