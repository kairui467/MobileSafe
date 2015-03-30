package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.service.AddressService;
import com.kerray.MobileSafe.service.CallSmsSafeService;
import com.kerray.MobileSafe.ui.SettingClickView;
import com.kerray.MobileSafe.ui.SettingItemView;
import com.kerray.MobileSafe.utils.ServiceUtils;

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
    // 设置是否开启自动更新
    private SettingItemView siv_update;
    private SharedPreferences sp;

    // 设置是否开启显示归属地
    private SettingItemView siv_show_address;
    private Intent showAddress;

    //设置归属地显示框背景
    private SettingClickView scv_changebg;

    //黑名单拦截设置
    private SettingItemView siv_callsms_safe;
    private Intent callSmsSafeIntent;

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

        // 设置号码归属地显示控件
        siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
        showAddress = new Intent(this, AddressService.class);
        boolean isServiceRunning = ServiceUtils.isServiceRunning(SettingActivity.this, "com.kerray.MobileSafe.service.AddressService");

        //监听来电的服务是开启的
        if (isServiceRunning)
            siv_show_address.setChecked(true);
        else
            siv_show_address.setChecked(false);

        siv_show_address.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (siv_show_address.isChecked())
                {
                    // 变为非选中状态
                    siv_show_address.setChecked(false);
                    stopService(showAddress);
                } else
                {
                    // 选择状态
                    siv_show_address.setChecked(true);
                    startService(showAddress);
                }
            }
        });

        //黑名单拦截设置
        siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);
        callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
        siv_callsms_safe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (siv_callsms_safe.isChecked())
                {
                    // 变为非选中状态
                    siv_callsms_safe.setChecked(false);
                    stopService(callSmsSafeIntent);
                } else
                {
                    // 选择状态
                    siv_callsms_safe.setChecked(true);
                    startService(callSmsSafeIntent);
                }

            }
        });

        //设置号码归属地的背景
        scv_changebg = (SettingClickView) findViewById(R.id.scv_changebg);
        scv_changebg.setTitle("归属地提示框风格");
        final String[] items = { "半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿" };
        int which = sp.getInt("which", 0);
        scv_changebg.setDesc(items[which]);

        scv_changebg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int dd = sp.getInt("which", 0);
                // 弹出一个对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("归属地提示框风格");
                builder.setSingleChoiceItems(items, dd, new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        //保存选择参数
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("which", which);
                        editor.commit();
                        scv_changebg.setDesc(items[which]);

                        //取消对话框
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("cancel", null);
                builder.show();
            }
        });
    }

    @Override
    protected void onResume()
    {
        siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
        showAddress = new Intent(this, AddressService.class);
        boolean isServiceRunning = ServiceUtils.isServiceRunning(SettingActivity.this, "com.kerray.MobileSafe.service.AddressService");

        //监听来电的服务是开启的
        if (isServiceRunning)
            siv_show_address.setChecked(true);
        else
            siv_show_address.setChecked(false);

        boolean iscallSmsServiceRunning = ServiceUtils.isServiceRunning(
          SettingActivity.this,
          "com.kerray.MobileSafe.service.CallSmsSafeService");
        siv_callsms_safe.setChecked(iscallSmsServiceRunning);

        super.onResume();
    }
}