package com.kerray.MobileSafe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.recriver.MyWidget;
import com.kerray.MobileSafe.utils.SystemInfoUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Created by kerray on 2015/4/6.
 * @方法名:com.kerray.MobileSafe.service
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/6
 */
public class UpdateWidgetService extends Service
{
    private ScreenOffReceiver offreceiver;
    private ScreenOnReceiver onreceiver;

    private Timer timer;
    private TimerTask task;
    private AppWidgetManager awm;           // widget的管理器

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
            Log.i("kerray", "屏幕锁屏了。。。");
            stopTimer();
        }
    }

    private class ScreenOnReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("kerray", "屏幕解锁了。。。");
            startTimer();
        }
    }

    private void startTimer()
    {
        if (timer == null && task == null)
        {
            timer = new Timer();
            task = new TimerTask()
            {
                public void run()
                {
                    Log.i("kerray", "更新widget");
                    // 设置更新的组件
                    ComponentName provider = new ComponentName(UpdateWidgetService.this, MyWidget.class);
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.process_widget);
                    views.setTextViewText(R.id.process_count, "正在运行的进程:" + SystemInfoUtils
                      .getRunningPocessCount(getApplicationContext()) + "个");
                    long size = SystemInfoUtils.getAvailMem(getApplicationContext());
                    views.setTextViewText(R.id.process_memory, "可用内存:" + Formatter.formatFileSize(
                      getApplicationContext(), size));
                    // 描述一个动作,这个动作是由另外的一个应用程序执行的.
                    // 自定义一个广播事件,杀死后台进度的事件
                    Intent intent = new Intent();
                    intent.setAction("com.kerray.MobileSafe.recriver.killall");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                      PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);

                    awm.updateAppWidget(provider, views);
                }
            };
            timer.schedule(task, 0, 3000);            // 参数1:执行哪个task，2:第一次执行等待时间，3:隔多久更新
        }
    }

    private void stopTimer()
    {
        if (timer != null && task != null)
        {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
    }

    @Override
    public void onCreate()
    {
        onreceiver = new ScreenOnReceiver();
        offreceiver = new ScreenOffReceiver();
        registerReceiver(onreceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(offreceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        awm = AppWidgetManager.getInstance(this);
        startTimer();
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(offreceiver);
        unregisterReceiver(onreceiver);
        offreceiver = null;
        onreceiver = null;
        stopTimer();
    }
}
