package com.kerray.MobileSafe.recriver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 * @Created by kerray on 2015/4/6.
 * @方法名:com.kerray.MobileSafe.recriver
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/6
 */
public class KillAllRecevier extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("kerray", "自定义的广播消息接收到了..");
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos)
            am.killBackgroundProcesses(info.processName);
    }
}
