package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.kerray.MobileSafe.R;

/**
 * @Created by kerray on 2015/3/18.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/18
 */
public class LostFindActivity extends Activity
{
    private SharedPreferences sp;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        //判断一下，是否做过设置向导，如果没有做过，就跳转到设置向导页面去设置，否则就留着当前的页面
        boolean configed = sp.getBoolean("configed", false);
        if(configed){
            // 就在手机防盗页面
            setContentView(R.layout.activity_lost_find);
        }else{
            //还没有做过设置向导
            Intent intent = new Intent(this,Setup1Activity.class);
            startActivity(intent);
            //关闭当前页面
            finish();
        }
    }

    /**
     * 重新进入手机防盗设置向导页面
     * @param view
     */
    public void reEnterSetup(View view){
        Intent intent = new Intent(this,Setup1Activity.class);
        startActivity(intent);
        //关闭当前页面
        finish();
    }
}