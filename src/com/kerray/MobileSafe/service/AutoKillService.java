package com.kerray.MobileSafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * @Created by kerray on 2015/4/6.
 * @方法名:com.kerray.MobileSafe.service
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/6
 */
public class AutoKillService extends Service
{
    private ScreenLockReceiver mScreenLockReceiver;

    private class ScreenLockReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("kerray", "屏幕锁屏了");
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses())
                am.killBackgroundProcesses(info.processName);
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mScreenLockReceiver = new ScreenLockReceiver();
        registerReceiver(mScreenLockReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        /*timer = new Timer();
        task = new TimerTask() {
			@Override
			public void run() {
				//自动清理内存
			}
		};
		timer.schedule(task, 0, 1000*60*60*2);

		CountDownTimer countDownTimer =  new CountDownTimer(1000*60*60*2, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {
				//countDownTimer.start();
			}
		};
		countDownTimer.start();*/
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mScreenLockReceiver);
        mScreenLockReceiver = null;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
