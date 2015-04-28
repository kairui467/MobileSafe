package com.kerray.MobileSafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.kerray.MobileSafe.activity.EnterPwdActivity;
import com.kerray.MobileSafe.db.dao.ApplockDao;

import java.util.List;

/**
 * @Created by kerray on 2015/4/7.
 * @方法名:com.kerray.MobileSafe.service
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/7
 */
public class WatchDogService extends Service
{
    private ActivityManager am;
    private boolean flag;
    private ApplockDao dao;
    private InnerReceiver innerReceiver;
    private String tempStopProtectPackname;
    private ScreenOffReceiver offreceiver;
    private DataChangeReceiver dataChangeReceiver;

    private List<String> protectPacknames;
    private Intent intent;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class ScreenOffReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("kerray", "锁屏清空   tempStopProtectPackname");
            tempStopProtectPackname = null;
        }
    }

    private class InnerReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("kerray", "接收到了临时停止保护的广播事件");
            tempStopProtectPackname = intent.getStringExtra("packname");
        }
    }

    private class DataChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("kerray", "数据库的内容变化了。。。");
            protectPacknames = dao.findAll();
        }
    }

    @Override
    public void onCreate()
    {
        offreceiver = new ScreenOffReceiver();
        registerReceiver(offreceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        innerReceiver = new InnerReceiver();
        registerReceiver(innerReceiver, new IntentFilter("com.kerray.MobileSafe.tempStop"));
        dataChangeReceiver = new DataChangeReceiver();
        registerReceiver(dataChangeReceiver, new IntentFilter("com.kerray.MobileSafe.applockchange"));

        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        dao = new ApplockDao(this);
        protectPacknames = dao.findAll();
        flag = true;
        // 当前应用需要保护。蹦出来，弹出来一个输入密码的界面。
        intent = new Intent(getApplicationContext(), EnterPwdActivity.class);
        // 服务是没有任务栈信息的，在服务开启activity，要指定这个activity运行的任务栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        new Thread()
        {
            public void run()
            {
                while (flag)
                {
                    List<ActivityManager.RunningTaskInfo> infos = am.getRunningTasks(1);
                    String packname = infos.get(0).topActivity.getPackageName();
                    //Log.i("kerra", "当前用户操作的应用程序:" + packname);          // 培训好了。
                    //if (dao.find(packname)){
                    //查询数据库太慢了，消耗资源，改成查询内存
                    if (protectPacknames.contains(packname))                      // 查询内存效率高很多
                    {
                        // 判断这个应用程序是否需要临时的停止保护。
                        if (packname.equals(tempStopProtectPackname))
                        {
                        } else
                        {
                            // 设置要保护程序的包名
                            intent.putExtra("packname", packname);
                            startActivity(intent);
                        }
                    }
                    try
                    {
                        Thread.sleep(20);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            ;
        }.start();
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        flag = false;
        unregisterReceiver(innerReceiver);
        innerReceiver = null;
        unregisterReceiver(offreceiver);
        offreceiver = null;
        unregisterReceiver(dataChangeReceiver);
        dataChangeReceiver = null;
        super.onDestroy();
    }
}
