package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.service.AutoKillService;
import com.kerray.MobileSafe.utils.ServiceUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * @Created by kerray on 2015/4/4.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/4
 */
public class TaskManagerSettingActivity extends Activity
{
    @ViewInject(R.id.cb_show_system)
    private CheckBox cb_show_system;
    @ViewInject(R.id.cb_lock_autokill)
    private CheckBox cb_lock_autokill;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager_setting);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        ViewUtils.inject(this);

        cb_show_system.setChecked(sp.getBoolean("showsystem", false));
        cb_show_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("showsystem", isChecked);
                editor.commit();
            }
        });

        final Intent i = new Intent(this, AutoKillService.class);
        cb_lock_autokill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                    startService(i);
                else
                    stopService(i);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (ServiceUtils.isServiceRunning(this, "com.kerray.MobileSafe.service.AutoKillService"))
            cb_lock_autokill.setChecked(true);
        else
            cb_lock_autokill.setChecked(false);
    }
}