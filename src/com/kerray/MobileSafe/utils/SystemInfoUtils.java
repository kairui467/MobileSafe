package com.kerray.MobileSafe.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

public class SystemInfoUtils
{
    /**
     * 获取手机的总内存大小 单位byte
     * @return
     */
    public static long getTotalMem()
    {
        try
        {
            FileInputStream fis = new FileInputStream("/proc/meminfo");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String totalInfo = br.readLine();
            //MemTotal:         513000 kB
            StringBuffer sb = new StringBuffer();
            for (char c : totalInfo.toCharArray())
                if (c >= '0' && c <= '9')
                    sb.append(c);
            long bytesize = Long.parseLong(sb.toString()) * 1024;
            return bytesize;
        } catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取可用的内存信息。
     * @param context
     * @return
     */
    public static long getAvailMem(Context context)
    {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取内存大小
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        long availMem = outInfo.availMem;
        return availMem;
    }

    /**
     * 得到正在运行的进程的数量
     * @param context
     * @return
     */
    public static int getRunningPocessCount(Context context)
    {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = am.getRunningAppProcesses();
        int count = runningAppProcessInfos.size();
        return count;
    }
}
