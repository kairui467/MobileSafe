package com.kerray.MobileSafe.service;

import android.app.Service;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.db.dao.NumberAddressQueryUtils;

/**
 * @Created by kerray on 2015/3/25.
 * @方法名:com.kerray.MobileSafe.service
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/25
 */
public class AddressService extends Service
{
    private WindowManager wm;                   // 窗体管理者
    private View view;

    private TelephonyManager tm;                // 电话服务
    private MyListenerPhone listenerPhone;

    private OutCallReceiver receiver;
    private WindowManager.LayoutParams params;
    private SharedPreferences sp;

    long[] mHits = new long[2];

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    // 服务里面的内部类
    // 广播接收者的生命周期和服务一样
    class OutCallReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // 这就是我们拿到的播出去的电话号码
            String phone = getResultData();
            // 查询数据库
            String address = NumberAddressQueryUtils.queryNumber(phone);

            myToast(address);
        }
    }

    /**
     * 自定义土司
     * @param address
     */
    private void myToast(String address)
    {
        view = View.inflate(this, R.layout.address_show, null);
        TextView textview = (TextView) view.findViewById(R.id.tv_address);

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500))
                {
                    // 双击居中了。。。
                    params.x = wm.getDefaultDisplay().getWidth() / 2 - view.getWidth() / 2;
                    wm.updateViewLayout(view, params);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("lastx", params.x);
                    editor.commit();
                }
            }
        });

        // 给view对象设置一个触摸的监听器
        view.setOnTouchListener(new View.OnTouchListener()
        {
            // 定义手指的初始化位置
            int startX;
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                case MotionEvent.ACTION_DOWN:           //手指按下屏幕
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    Log.i("kerray", "手指摸到控件=" + startX + "," + startY);
                    break;
                case MotionEvent.ACTION_MOVE:           //手指在屏幕上移动
                    int newX = (int) event.getRawX();
                    int newY = (int) event.getRawY();
                    int dx = newX - startX;
                    int dy = newY - startY;
                    Log.i("kerray", "手指在控件上移动" + dx + "," + dy);
                    params.x += dx;
                    params.y += dy;

                    // 考虑边界问题
                    params.x = (params.x < 0) ? 0 : params.x;
                    params.y = (params.y < 0) ? 0 : params.y;
                    int wx = wm.getDefaultDisplay().getWidth() - view.getWidth();
                    int wy = wm.getDefaultDisplay().getHeight() - view.getHeight();
                    params.x = (params.x > wx) ? wx : params.x;
                    params.y = (params.y > wy) ? wy : params.y;

                    wm.updateViewLayout(view, params);
                    // 重新初始化手指开始结束的位置
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:             //手指离开屏幕
                    // 记录控件距离屏幕左上角的坐标
                    Log.i("kerray", "手指离开控件");
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("lastx", params.x);
                    editor.putInt("lasty", params.y);
                    editor.commit();
                    break;
                }
                return false;// 事件处理完毕了。不要让父控件 父布局响应触摸事件了。
            }
        });

        //"半透明","活力橙","卫士蓝","金属灰","苹果绿"
        int[] ids = { R.drawable.call_locate_white, R.drawable.call_locate_orange, R.drawable.call_locate_blue
          , R.drawable.call_locate_gray, R.drawable.call_locate_green };
        sp = getSharedPreferences("config", MODE_PRIVATE);
        view.setBackgroundResource(ids[sp.getInt("which", 0)]);
        textview.setText(address);
        //窗体的参数就设置好了
        params = new WindowManager.LayoutParams();

        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // 与窗体左上角对其
        params.gravity = Gravity.TOP + Gravity.LEFT;
        // 指定窗体距离左边100 上边100个像素
        params.x = sp.getInt("lastx", 0);
        params.y = sp.getInt("lasty", 0);

        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
          | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        // android系统里面具有电话优先级的一种窗体类型，记得添加权限。
        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        wm.addView(view, params);
    }

    private class MyListenerPhone extends PhoneStateListener
    {
        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            // state：状态，incomingNumber：来电号码
            super.onCallStateChanged(state, incomingNumber);
            switch (state)
            {
            case TelephonyManager.CALL_STATE_RINGING:// 来电铃声响起
                // 查询数据库的操作
                String address = NumberAddressQueryUtils
                  .queryNumber(incomingNumber);

                myToast(address);
                break;

            case TelephonyManager.CALL_STATE_IDLE://电话的空闲状态：挂电话、来电拒绝
                //把这个View移除
                if (view != null)
                    wm.removeView(view);
                break;

            default:
                break;
            }
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        // 监听来电
        listenerPhone = new MyListenerPhone();
        tm.listen(listenerPhone, PhoneStateListener.LISTEN_CALL_STATE);
        //用代码去注册广播接收者
        receiver = new OutCallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(receiver, filter);

        //实例化窗体
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // 取消监听来电
        tm.listen(listenerPhone, PhoneStateListener.LISTEN_NONE);
        listenerPhone = null;

        //用代码取消注册广播接收者
        unregisterReceiver(receiver);
        receiver = null;
    }
}
