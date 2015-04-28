package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.*;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.db.dao.AntivirsuDao;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;

/**
 * @Created by kerray on 2015/4/9.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/9
 */
public class AntiVirusActivity extends Activity
{
    protected static final int SCANING = 0;
    protected static final int FINISH = 2;

    @ViewInject(R.id.iv_scan)
    private ImageView iv_scan;
    @ViewInject(R.id.progressBar1)
    private ProgressBar progressBar1;
    @ViewInject(R.id.tv_scan_status)
    private TextView tv_scan_status;
    @ViewInject(R.id.ll_container)
    private LinearLayout ll_container;

    private PackageManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_virus);
        ViewUtils.inject(this);

        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
          Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(1000);
        ra.setRepeatCount(Animation.INFINITE);
        iv_scan.startAnimation(ra);

        progressBar1.setMax(100);
        scanVirus();
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case SCANING:
                ScanInfo scanInfo = (ScanInfo) msg.obj;
                tv_scan_status.setText("正在扫描" + scanInfo.name);

                TextView tv = new TextView(getApplicationContext());
                tv.setHorizontallyScrolling(true);
                if (scanInfo.isvirus)
                {
                    tv.setTextColor(Color.RED);
                    tv.setText("发现病毒：" + scanInfo.packname);
                } else
                {
                    tv.setTextColor(Color.BLACK);
                    tv.setText("扫描安全：" + scanInfo.packname);
                }
                ll_container.addView(tv, 0);
                break;
            case FINISH:
                tv_scan_status.setText("扫描完毕");
                iv_scan.clearAnimation();
                break;
            }
        }
    };


    /**
     * 扫描信息的内部类
     */
    class ScanInfo
    {
        String packname;
        String name;
        boolean isvirus;
    }

    /**
     * 扫描病毒
     */
    private void scanVirus()
    {
        pm = getPackageManager();
        tv_scan_status.setText("正在初始化杀毒引擎。。。");
        new Thread()
        {
            @Override
            public void run()
            {
                List<PackageInfo> infos = pm.getInstalledPackages(0);
                progressBar1.setMax(infos.size());
                int progress = 0;
                for (PackageInfo info : infos)
                {
                    // apk文件的完整路径
                    String sourcedir = info.applicationInfo.sourceDir;//apk apk图片
                    // zip包
                    String md5 = getFileMd5(sourcedir);
                    ScanInfo scanInfo = new ScanInfo();
                    scanInfo.name = info.applicationInfo.loadLabel(pm).toString();
                    scanInfo.packname = info.packageName;
                    Log.i("kerray", scanInfo.packname + ":" + md5);
                    //查询md5信息，是否在病毒数据库里面存在
                    if (AntivirsuDao.isVirus(md5))
                        scanInfo.isvirus = true;        //发现病毒
                    else
                        scanInfo.isvirus = false;       //扫描安全

                    Message msg = Message.obtain();
                    msg.obj = scanInfo;
                    msg.what = SCANING;
                    handler.sendMessage(msg);
                    try
                    {
                        sleep(100);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    progress++;
                    progressBar1.setProgress(progress);
                }
                Message msg = Message.obtain();
                msg.what = FINISH;
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 获取文件的md5值
     * @param path 文件的全路径名称
     * @return
     */
    private String getFileMd5(String path)
    {
        try
        {
            // 获取一个文件的特征信息，签名信息。
            File file = new File(path);
            // md5
            MessageDigest digest = MessageDigest.getInstance("md5");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) != -1)
                digest.update(buffer, 0, len);

            byte[] result = digest.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : result)
            {
                // 与运算
                int number = b & 0xff;// 加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1)
                    sb.append("0");

                sb.append(str);
            }
            return sb.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
}