package com.kerray.MobileSafe.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * @Created by kerray on 2015/3/25.
 * @方法名:com.kerray.MobileSafe.utils
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/25
 */
public class ServiceUtils
{
    /**
     * 校验某个服务是否还活着
     * serviceName :传进来的服务的名称
     */
    public static boolean isServiceRunning(Context context, String serviceName)
    {
        //校验服务是否还活着
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : infos)
        {
            String name = info.service.getClassName();
            Log.i("kerray", "ServerName---->" + name);
            if (serviceName.equals(name))
            {
                return true;
            }
        }
        return false;
    }
}
