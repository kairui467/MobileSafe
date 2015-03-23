package com.kerray.MobileSafe.recriver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.service.GPSService;

/**
 * @Created by kerray on 2015/3/22.
 * @方法名:com.kerray.MobileSafe.recriver
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/22
 */
public class SMSReceiver extends BroadcastReceiver
{
    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // 写接收短信的代码
        Object[] objs = (Object[]) intent.getExtras().get("pdus");
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);

        for (Object b : objs)
        {
            //具体的某一条短信
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) b);
            //发送者
            String sender = sms.getOriginatingAddress();            //15555555556
            String safenumber = sp.getString("safenumber", "");     //5556
            Log.i("kerray", "====sender==" + sender);
            String body = sms.getMessageBody();

            if (sender.contains(safenumber))
            {
                if ("#*location*#".equals(body))
                {
                    //得到手机的GPS
                    Log.i("kerray", "得到手机的GPS");

                    //启动服务
                    Intent i = new Intent(context, GPSService.class);
                    context.startService(i);

                    SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                    String lastlocation = sp.getString("lastlocation", null);
                    Log.i("kerray", "lastlocation=" + lastlocation);
                    if (TextUtils.isEmpty(lastlocation))
                        SmsManager.getDefault().sendTextMessage(sender, null, "geting loaction.....", null, null);
                    else
                        SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);

                    //把这个广播终止掉
                    abortBroadcast();
                } else if ("#*alarm*#".equals(body))
                {
                    //播放报警影音
                    Log.i("kerray", "播放报警影音");
                    MediaPlayer play = MediaPlayer.create(context, R.raw.mi);
                    play.setLooping(false);
                    play.setVolume(1.0f, 1.0f);
                    play.start();

                    abortBroadcast();
                } else if ("#*wipedata*#".equals(body))
                {
                    //远程清除数据
                    Log.i("kerray", "远程清除数据");
                    abortBroadcast();
                } else if ("#*lockscreen*#".equals(body))
                {
                    //远程锁屏
                    Log.i("kerray", "远程锁屏");
                    abortBroadcast();
                }
            }
        }
    }
}
