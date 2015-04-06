package com.kerray.MobileSafe.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.domain.TaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务信息 & 进程信息的解析器
 */
public class TaskInfoParser
{
    /**
     * 获取正在运行的所有的进程的信息。
     * @param pContext 上下文
     * @return 进程信息的集合
     */
    public static List<TaskInfo> getRunningTaskInfos(Context pContext)
    {
        ActivityManager am = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = pContext.getPackageManager();
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos)
        {
            TaskInfo taskInfo = new TaskInfo();
            String packname = processInfo.processName;

            Debug.MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(new int[] { processInfo.pid });
            long memsize = memoryInfos[0].getTotalPrivateDirty() * 1024;

            taskInfo.setPackname(packname);
            taskInfo.setMemsize(memsize);
            try
            {
                PackageInfo packageInfo = pm.getPackageInfo(packname, 0);
                Drawable icon = packageInfo.applicationInfo.loadIcon(pm);
                String appname = packageInfo.applicationInfo.loadLabel(pm).toString();
                taskInfo.setIcon(icon);
                taskInfo.setAppname(appname);

                if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0)
                    taskInfo.setUsertask(false);            //系统进程
                else
                    taskInfo.setUsertask(true);             //用户进程
            } catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
                taskInfo.setAppname(packname);
                taskInfo.setIcon(pContext.getResources().getDrawable(R.drawable.ic_default));
            }
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }
}
