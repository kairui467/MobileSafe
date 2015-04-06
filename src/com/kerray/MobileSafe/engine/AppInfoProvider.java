package com.kerray.MobileSafe.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.kerray.MobileSafe.domain.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务方法，提供手机里面安装的所有的应用程序信息
 */
public class AppInfoProvider
{
    /**
     * 获取所有的安装的应用程序信息。
     * @param pContext 上下文
     * @return
     */
    public static List<AppInfo> getAppInfos(Context pContext)
    {
        PackageManager pm = pContext.getPackageManager();
        //所有的安装在系统上的应用程序包信息。
        List<PackageInfo> packInfos = pm.getInstalledPackages(0);
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        for (PackageInfo packInfo : packInfos)
        {
            AppInfo mAppInfo = new AppInfo();
            //packInfo  相当于一个应用程序apk包的清单文件
            String packname = packInfo.packageName;
            Drawable icon = packInfo.applicationInfo.loadIcon(pm);
            String name = packInfo.applicationInfo.loadLabel(pm).toString();
            //应用程序apk包的路径
            String apkpath = packInfo.applicationInfo.sourceDir;
            File file = new File(apkpath);
            long appSize = file.length();

            int flags = packInfo.applicationInfo.flags;//应用程序信息的标记 相当于用户提交的答卷
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                mAppInfo.setUserApp(true);          //用户程序
            else
                mAppInfo.setUserApp(false);         //系统程序

            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0)
                mAppInfo.setInRom(true);            //手机的内存
            else
                mAppInfo.setInRom(false);           //手机外存储设备

            mAppInfo.setPackname(packname);
            mAppInfo.setIcon(icon);
            mAppInfo.setName(name);
            mAppInfo.setAppSize(appSize);
            appInfos.add(mAppInfo);
        }
        return appInfos;
    }
}
