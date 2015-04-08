package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.kerray.MobileSafe.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * @Created by kerray on 2015/4/7.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/7
 */
public class EnterPwdActivity extends Activity
{
    @ViewInject(R.id.et_password)
    private EditText et_password;
    @ViewInject(R.id.tv_name)
    private TextView tv_name;
    @ViewInject(R.id.iv_icon)
    private ImageView iv_icon;

    private String packname;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pwd);
        ViewUtils.inject(this);

        Intent intent = getIntent();
        //  当前要保护的应用程序包名
        packname = intent.getStringExtra("packname");

        PackageManager pm = getPackageManager();
        try
        {
            ApplicationInfo info = pm.getApplicationInfo(packname, 0);
            tv_name.setText(info.loadLabel(pm));
            iv_icon.setImageDrawable(info.loadIcon(pm));
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void onBackPressed()
    {
        //回桌面。
        //<action android:name="android.intent.action.MAIN" />
        //<category android:name="android.intent.category.HOME" />
        //<category android:name="android.intent.category.DEFAULT" />
        //<category android:name="android.intent.category.MONKEY"/>
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.MONKEY");
        startActivity(intent);
        //所有的activity最小化 不会执行ondestory 只执行 onstop方法。
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        finish();
    }

    public void click(View view)
    {
        String pwd = et_password.getText().toString().trim();
        if (TextUtils.isEmpty(pwd))
        {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //假设正确的密码是123
        if ("1".equals(pwd))
        {
            //告诉看门狗这个程序密码输入正确了。 可以临时的停止保护。
            //自定义的广播,临时停止保护。
            Intent intent = new Intent();
            intent.setAction("com.kerray.MobileSafe.tempStop");
            intent.putExtra("packname", packname);
            sendBroadcast(intent);
            finish();
        } else
            Toast.makeText(this, "密码错误。。", Toast.LENGTH_SHORT).show();
    }
}