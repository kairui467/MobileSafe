package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.*;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.kerray.MobileSafe.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @Created by kerray on 2015/4/9.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/9
 */
public class CleanCacheActivity extends Activity
{
    @ViewInject(R.id.pb)
    private ProgressBar pb;
    @ViewInject(R.id.tv_scan_status)
    private TextView tv_scan_status;
    @ViewInject(R.id.ll_container)
    private LinearLayout ll_container;

    private PackageManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_cache);
        ViewUtils.inject(this);

        scanCache();
    }

    /**
     * 扫描手机里面所有应用程序的缓存信息
     */
    private void scanCache()
    {
        pm = getPackageManager();
        new Thread()
        {
            public void run()
            {

                //复制到和 Activity 同目录下就会重新编译了，再删除就是了，感谢！
                //Method getPackageSizeInfoMethod = null;
                /*Method[] methods = PackageManager.class.getMethods();
                for (Method method : methods)
                {
                    if ("getPackageSizeInfo".equals(method.getName()))
                    {
                        getPackageSizeInfoMethod = method;
                    }
                }*/
                List<PackageInfo> packInfos = pm.getInstalledPackages(0);
                pb.setMax(packInfos.size());
                Log.i("kerray", "packInfos大小：" + packInfos.size());
                int progress = 0;
                Method getPackageSizeInfo = null;
                try
                {
                    getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                } catch (NoSuchMethodException e)
                {
                    e.printStackTrace();
                }
                for (PackageInfo packInfo : packInfos)
                {
                    try
                    {
                        //getPackageSizeInfoMethod.invoke(pm, packInfo.packageName, new MyDataObserver());

                        getPackageSizeInfo.invoke(pm, packInfo.packageName, new MyDataObserver());
                        Thread.sleep(50);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    progress++;
                    pb.setProgress(progress);
                }
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        tv_scan_status.setText("扫描完毕...");
                    }
                });
            }
        }.start();
    }

    int i = 0;

    private class MyDataObserver extends IPackageStatsObserver.Stub
    {
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException
        {
            final long cache = pStats.cacheSize;
            long code = pStats.codeSize;
            long data = pStats.dataSize;
            final String packname = pStats.packageName;
            String appName = null;
            try
            {
                appName = pm.getApplicationInfo(packname, 0).loadLabel(pm).toString();
            } catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }
            /*i++;
            Log.i("kerray", i + "、" + appName + "，packname=" + packname);
            Log.i("kerray", "code=" + Formatter.formatFileSize(getApplicationContext(), code));
            Log.i("kerray", "data=" + Formatter.formatFileSize(getApplicationContext(), data));
            Log.i("kerray", "cache=" + Formatter.formatFileSize(getApplicationContext(), cache));
            Log.i("kerray", "**************************************");*/
            final ApplicationInfo appinfo;
            try
            {
                appinfo = pm.getApplicationInfo(packname, 0);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        tv_scan_status.setText("正在扫描：" + appinfo.loadLabel(pm));
                        if (cache > 0)
                        {
                            View view = View.inflate(getApplicationContext(), R.layout.list_item_cacheinfo, null);
                            TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache_size);
                            tv_cache.setText("缓存大小:" + Formatter.formatFileSize(getApplicationContext(), cache));
                            TextView tv_name = (TextView) view.findViewById(R.id.tv_app_name);
                            tv_name.setText(appinfo.loadLabel(pm));

                            ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
                            iv_delete.setOnClickListener(new View.OnClickListener()
                            {
                                public void onClick(View v)
                                {
                                    try
                                    {
                                        Method method = PackageManager.class.getMethod("deleteApplicationCacheFiles", String.class, IPackageDataObserver.class);
                                        method.invoke(pm, packname, new MyPackDataObserver());
                                    } catch (NoSuchMethodException e)
                                    {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e)
                                    {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            ll_container.addView(view, 0);
                        }
                    }
                });
            } catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class MyPackDataObserver extends IPackageDataObserver.Stub
    {
        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException
        {
            Log.i("kerray", packageName + "======" + succeeded);
        }
    }

    /**
     * 清理手机的全部缓存.
     * @param view
     */
    public void clearAll(View view)
    {
        /*Method[] methods = PackageManager.class.getMethods();
        for (Method method : methods)
        {
            if ("freeStorageAndNotify".equals(method.getName()))
            {
                try
                {
                    method.invoke(pm, Integer.MAX_VALUE, new MyPackDataObserver());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }
        }*/

        Method method = null;
        try
        {
            method = PackageManager.class.getMethod("freeStorageAndNotify", Intent.class, IPackageDataObserver.class);
            method.invoke(pm, Integer.MAX_VALUE, new MyPackDataObserver());
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
}