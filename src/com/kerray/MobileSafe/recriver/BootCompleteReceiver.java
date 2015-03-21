package com.kerray.MobileSafe.recriver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @Created by kerray on 2015/3/20.
 * @方法名:com.kerray.MobileSafe.recriver
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/20
 */
public class BootCompleteReceiver extends BroadcastReceiver
{
    private SharedPreferences sp;
    private TelephonyManager tm;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);

        boolean protecting = sp.getBoolean("protecting", false);

        if (protecting)
        {
            //开启防盗保护才执行这个地方
            tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // 读取之前保存的SiM信息；
            String saveSim = sp.getString("sim", "")+"dkfmskljsjklm";

            //读取当前的sim卡信息
            String realSim = tm.getSimSerialNumber();

            //比较是否一样
            if (saveSim.equals(realSim))
            {
                //sim没有变更，还是同一个哥们
            } else
            {
                // sim 已经变更 发一个短信给安全号码
                Log.i("kerray", "sim 卡已经变更");
                Toast.makeText(context, "sim 卡已经变更", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
