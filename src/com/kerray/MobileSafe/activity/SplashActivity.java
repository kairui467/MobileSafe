package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.utils.StreamTools;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
    private String description;                     //描述信息
    private String apkurl;                          //新版本的下载地址
    private boolean update;                         //是否自动升级

    @ViewInject(R.id.tv_update_info)
    private TextView tv_update_info;
    @ViewInject(R.id.tv_splash_version)
    private TextView tv_splash_version;

    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ViewUtils.inject(this);

        tv_splash_version.setText("版本号：" + getVersionName());
        tv_update_info.setVisibility(View.GONE);

        initSharedPreferences();
        installShortCut();

        //拷贝数据库
        copyDB("address.db");
        copyDB("antivirus.db");

        if (update)
            //检测升级
            checkUpdate();
        else
        {   //自动升级已经关闭
            handler.postDelayed(new Runnable()
            {
                public void run()
                {   //进入主页面
                    enterHome();
                }
            }, 2000);
        }

        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(1000);
        findViewById(R.id.rl_root_splash).startAnimation(aa);
    }

    /**
     * 创建快捷图标
     */
    private void installShortCut()
    {
        boolean shortcut = sp.getBoolean("shortcut", false);
        if (shortcut)
            return;

        SharedPreferences.Editor editor = sp.edit();
        //发送广播的意图， 大吼一声告诉桌面，要创建快捷图标了
        Intent intent = new Intent();
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        //快捷方式  要包含3个重要的信息 1，名称 2.图标 3.干什么事情
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机小卫士");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launche));
        //桌面点击图标对应的意图。
        Intent shortcutIntent = new Intent();
        shortcutIntent.setAction("android.intent.action.MAIN");
        shortcutIntent.addCategory("android.intent.category.LAUNCHER");
        shortcutIntent.setClassName(getPackageName(), "com.kerray.MobileSafe.activity.SplashActivity");
        //shortcutIntent.setAction("com.itheima.xxxx");
        //shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        sendBroadcast(intent);

        editor.putBoolean("shortcut", true);
        editor.commit();
    }

    /**
     * //path 把address.db这个数据库拷贝到data/data/《包名》/files/address.db
     */
    private void copyDB(String pFileName)
    {
        //只要你拷贝了一次，我就不要你再拷贝了
        try
        {
            File file = new File(getFilesDir(), pFileName);
            if (file.exists() && file.length() > 0)
            {
                //正常了，就不需要拷贝了
                Log.i(TAG, pFileName + "正常了，就不需要拷贝了");
            } else
            {
                InputStream is = getAssets().open(pFileName);

                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1)
                    fos.write(buffer, 0, len);

                is.close();
                fos.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 初始化 SharedPreferences
     */
    private void initSharedPreferences()
    {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        update = sp.getBoolean("update", false);

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("update", false);
    }

    //检查是否有新版本，如果有就升级
    private void checkUpdate()
    {
        new Thread()
        {
            public void run()
            {
                // URLhttp://192.168.1.101:8080/updateinfo.html
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
                        description = (String) obj.get("description");
                        apkurl = (String) obj.get("apkurl");

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
            case ENTER_HOME:        // 进入主页面
                enterHome();
                break;
            case URL_ERROR:         // URL错误
                enterHome();
                Toast.makeText(SplashActivity.this, "URL错误", Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_ERROR:     // 网络异常
                enterHome();
                Toast.makeText(SplashActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                break;
            case JSON_ERROR:        // JSON解析出错
                enterHome();
                Toast.makeText(SplashActivity.this, "JSON解析出错", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
            }
        }
    };

    //弹出升级对话框
    private void showUpdateDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
        dialog.setTitle("提示升级");
        dialog.setMessage(description);
        //        dialog.setCancelable(false);//强制升级
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface dialog)
            {
                //进入主页面
                enterHome();
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("立即升级", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // 下载APK，并且替换安装
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    HttpUtils http = new HttpUtils();
                    HttpHandler handler = http.download(apkurl, Environment.getExternalStorageDirectory().getAbsolutePath() + "/mobilesafe2.0.apk", false, false, new RequestCallBack<File>()
                    {
                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo)
                        {
                            installAPK(responseInfo.result.getAbsoluteFile());
                        }

                        @Override
                        public void onFailure(HttpException e, String s)
                        {
                            Toast.makeText(SplashActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoading(long total, long current, boolean isUploading)
                        {
                            super.onLoading(total, current, isUploading);
                            tv_update_info.setVisibility(View.VISIBLE);
                            //当前下载百分比
                            long progress = current * 100 / total;
                            tv_update_info.setText("下载进度：" + progress + "%");
                        }

                        @Override
                        public void onStart()
                        {
                            super.onStart();
                            tv_update_info.setText("开始下载！");
                        }

                        /**
                         * 安装APK
                         * @param file
                         */
                        private void installAPK(File file)
                        {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    });
                } else
                {
                    Toast.makeText(SplashActivity.this, "没有sdcard，请安装上在试", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        dialog.setNegativeButton("下次再说", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                enterHome();//进入主页面
            }
        });
        dialog.show();
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
