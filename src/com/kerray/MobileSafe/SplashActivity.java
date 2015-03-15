package com.kerray.MobileSafe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;
import com.kerray.MobileSafe.utils.StreamTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends Activity
{

    protected static final int SHOW_UPDATE_DIALOG = 0;
    protected static final int ENTER_HOME = 1;
    protected static final int URL_ERROR = 2;
    protected static final int NETWORK_ERROR = 3;
    protected static final int JSON_ERROR = 4;
    private static final String TAG = "kerray";
    private TextView tv_splash_version;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("版本号：" + getVersionName());

        checkUpdate();

        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(1000);
        findViewById(R.id.rl_root_splash).startAnimation(aa);
    }

    //检查是否有新版本，如果有就升级
    private void checkUpdate()
    {
        new Thread()
        {
            public void run()
            {
                // URLhttp://192.168.1.254:8080/updateinfo.html
                Message mes = Message.obtain();
                long startTime = System.currentTimeMillis();
                try
                {
                    URL url = new URL(getString(R.string.serverurl));
                    //联网
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(4000);
                    if (conn.getResponseCode() == 200)
                    {
                        //联网成功
                        InputStream is = conn.getInputStream();
                        //把流转成String
                        String result = StreamTools.readFromStream(is);
                        Log.i(TAG, "联网成功了" + result);

                        //json解析
                        JSONObject obj = new JSONObject(result);
                        // 得到服务器的版本信息
                        String verson = (String) obj.get("version");
                        String description = (String) obj.get("description");
                        String apkurl = (String) obj.get("apkurl");

                        // 校验是否有新版本
                        if (getVersionName().equals(verson))
                            // 版本一致，没有新版本，进入主页面
                            mes.what = ENTER_HOME;
                        else
                            // 有新版本，弹出一升级对话框
                            mes.what = SHOW_UPDATE_DIALOG;
                    }
                } catch (MalformedURLException e)
                {
                    mes.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e)
                {
                    mes.what = NETWORK_ERROR;
                    e.printStackTrace();
                } catch (JSONException e)
                {
                    mes.what = JSON_ERROR;
                    e.printStackTrace();
                } finally
                {
                    long endTime = System.currentTimeMillis();
                    // 我们花了多少时间
                    long dTime = endTime - startTime;
                    // 2000
                    if (dTime < 1500)
                        try
                        {
                            Thread.sleep(1500 - dTime);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    handler.sendMessage(mes);
                }
            }
        }.start();
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
            case SHOW_UPDATE_DIALOG:// 显示升级的对话框
                Log.i(TAG, "显示升级的对话框");
                showUpdateDialog();
                break;
            case ENTER_HOME:// 进入主页面
                enterHome();
                break;
            case URL_ERROR:// URL错误
                enterHome();
                Toast.makeText(getApplicationContext(), "URL错误", Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_ERROR:// 网络异常
                enterHome();
                Toast.makeText(SplashActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                break;
            case JSON_ERROR:// JSON解析出错
                enterHome();
                Toast.makeText(SplashActivity.this, "JSON解析出错", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
            }
        }
    };

    private void showUpdateDialog()
    {

    }

    private void enterHome()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        // 关闭当前页面
        finish();
    }

    //得到应用程序的版本名称
    public String getVersionName()
    {
        //用来管理手机的APK
        PackageManager pm = getPackageManager();
        try
        {
            //得到APK的功能清单文件
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
