package com.kerray.MobileSafe.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @Created by kerray on 2015/3/22.
 * @方法名:com.kerray.MobileSafe.service
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/22
 */
public class GPSService extends Service
{
    // 用到位置服务
    private LocationManager lm;
    private MyLocationListener listener;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        // List<String> provider = lm.getAllProviders();
        // for(String l: provider)
        //  System.out.println(l);
        listener = new MyLocationListener();
        // 注册监听位置服务
        // 给位置提供者设置条件
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        // 设置参数细化：
        // criteria.setAccuracy(Criteria.ACCURACY_FINE);//设置为最大精度
        // criteria.setAltitudeRequired(false);//不要求海拔信息
        // criteria.setBearingRequired(false);//不要求方位信息
        // criteria.setCostAllowed(true);//是否允许付费
        // criteria.setPowerRequirement(Criteria.POWER_LOW);//对电量的要求

        String proveder = lm.getBestProvider(criteria, true);
        lm.requestLocationUpdates(proveder, 0, 0, listener);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // 取消监听位置服务
        lm.removeUpdates(listener);
        listener = null;
    }

    class MyLocationListener implements LocationListener
    {
        /**
         * 当位置改变的时候回调
         */
        @Override
        public void onLocationChanged(Location location)
        {
            String longitude = "j:" + location.getLongitude() + "\n";
            String latitude = "w:" + location.getLatitude() + "\n";
            String accuracy = "a" + location.getAccuracy() + "\n";
            // 发短信给安全号码

            // 把标准的GPS坐标转换成火星坐标
            //			InputStream is;
            //			try {
            //				is = getAssets().open("axisoffset.dat");
            //				ModifyOffset offset = ModifyOffset.getInstance(is);
            //				PointDouble double1 = offset.s2c(new PointDouble(location
            //						.getLongitude(), location.getLatitude()));
            //				longitude ="j:" + offset.X+ "\n";
            //				latitude =  "w:" +offset.Y+ "\n";
            //
            //			} catch (IOException e) {
            //				// TODO Auto-generated catch block
            //				e.printStackTrace();
            //			} catch (Exception e) {
            //				// TODO Auto-generated catch block
            //				e.printStackTrace();
            //			}

            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("lastlocation", longitude + latitude + accuracy);
            editor.commit();
        }

        /**
         * 当状态发生改变的时候回调 开启--关闭 ；关闭--开启
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        /**
         * 某一个位置提供者可以使用了
         */
        @Override
        public void onProviderEnabled(String provider)
        {

        }

        /**
         * 某一个位置提供者不可以使用了
         */
        @Override
        public void onProviderDisabled(String provider)
        {

        }
    }
}
