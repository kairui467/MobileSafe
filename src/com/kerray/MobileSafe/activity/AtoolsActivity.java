package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.utils.SmsUtils;

/**
 * @Created by kerray on 2015/3/30.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/30
 */
public class AtoolsActivity extends Activity
{
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atools);
    }

    /**
     * 点击事件，进入号码归属地查询的页面
     * @param view
     */
    public void numberQuery(View view)
    {
        Intent intentv = new Intent(this, NumberAddressQueryActivity.class);
        startActivity(intentv);
    }

    /**
     * 点击事件，短信的备份
     * @param view
     */
    public void smsBackup(View view)
    {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage("正在备份短信");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        new Thread()
        {
            public void run()
            {
                try
                {
                    SmsUtils.backupSms(getApplicationContext(), new SmsUtils.BackUpCallBack()
                    {
                        @Override
                        public void beforeBackup(int max)
                        {
                            mProgressDialog.setMax(max);
                        }

                        @Override
                        public void onSmsBackup(int progress)
                        {
                            mProgressDialog.setProgress(progress);
                        }
                    });
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(AtoolsActivity.this, "备份成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e)
                {
                    e.printStackTrace();
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(AtoolsActivity.this, "备份失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally
                {
                    mProgressDialog.dismiss();
                }
            }
        }.start();
    }

    /**
     * 点击事件，短信的还原
     * @param view
     */
    public void smsRestore(View view)
    {
        SmsUtils.restoreSms(this, false);
        Toast.makeText(this, "还原成功", Toast.LENGTH_SHORT).show();
    }
}