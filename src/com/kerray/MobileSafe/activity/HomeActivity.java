package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.utils.MD5Utils;

/**
 * @Created by kerray on 2015/3/15.
 * @方法名:com.kerray.MobileSafe
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/15
 */
public class HomeActivity extends Activity
{
    private static final String TAG = "kerray";
    private GridView list_home;
    private MyAdapter adapter;
    private SharedPreferences sp;

    private EditText et_setup_pwd;
    private EditText et_setup_confirm;
    private Button ok;
    private Button cancel;
    private AlertDialog dialog;


    private static String[] names = {
      "手机防盗", "通讯卫士", "软件管理",
      "进程管理", "流量统计", "手机杀毒",
      "缓存清理", "高级工具", "设置中心"

    };

    private static int[] ids = {
      R.drawable.safe, R.drawable.callmsgsafe, R.drawable.app,
      R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
      R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings

    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        list_home = (GridView) findViewById(R.id.list_home);
        sp = getSharedPreferences("config", MODE_PRIVATE);

        adapter = new MyAdapter();
        list_home.setAdapter(adapter);

        list_home.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                case 0://进入手机防盗页面
                    showLostFindDialog();
                    break;
                case 7://进入高级工具
                    startActivity(new Intent(HomeActivity.this, NumberAddressQueryActivity.class));
                    break;
                case 8://进入设置中心
                    startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                    break;
                default:
                    break;
                }
            }
        });
    }


    private void showLostFindDialog()
    {
        //判断是否设置过密码
        if (isSetupPwd())
            //已经设置密码了，弹出的是输入对话框
            showEnterDialog();
        else
            //没有设置密码，弹出的是设置密码对话框
            showSetupPwdDialog();
    }

    /**
     * 设置密码对话框
     */
    private void showSetupPwdDialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        // 自定义一个布局文件
        View view = View.inflate(HomeActivity.this, R.layout.dialog_setup_password, null);
        et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
        et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //  取出密码
                String password = et_setup_pwd.getText().toString().trim();
                String password_confirm = et_setup_confirm.getText().toString().trim();
                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm))
                {
                    Toast.makeText(HomeActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                //判断是否一致才去保存
                if (password.equals(password_confirm))
                {
                    //一致的话，就保存密码，把对话框消掉，还要进入手机防盗页面
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("password", MD5Utils.md5Password(password));//保存加密后的
                    editor.commit();
                    dialog.dismiss();
                    Log.i(TAG, "一致的话，就保存密码，把对话框消掉，还要进入手机防盗页面");
                    Intent intent = new Intent(HomeActivity.this, LostFindActivity.class);
                    startActivity(intent);
                } else
                {
                    Toast.makeText(HomeActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        dialog = builder.create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 输入密码对话框
     */
    private void showEnterDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        // 自定义一个布局文件
        View view = View.inflate(HomeActivity.this, R.layout.dialog_enter_password, null);
        et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //把这个对话框取消掉
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //  取出密码
                String password = et_setup_pwd.getText().toString().trim();
                String savePassword = sp.getString("password", "");//取出加密后的
                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(HomeActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (MD5Utils.md5Password(password).equals(savePassword))
                {
                    //输入的密码是我之前设置的密码
                    //把对话框消掉，进入主页面；
                    dialog.dismiss();
                    Log.i(TAG, "把对话框消掉，进入手机防盗页面");
                    Intent intent = new Intent(HomeActivity.this, LostFindActivity.class);
                    startActivity(intent);

                } else
                {
                    Toast.makeText(HomeActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    et_setup_pwd.setText("");
                    return;
                }
            }
        });
        dialog = builder.create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 判断是否设置过密码
     * @return
     */
    private boolean isSetupPwd()
    {
        String password = sp.getString("password", null);
        return !TextUtils.isEmpty(password);
    }

    private class MyAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return names.length;
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = View.inflate(HomeActivity.this, R.layout.list_item_home, null);
            ImageView iv_item = (ImageView) view.findViewById(R.id.iv_item);
            TextView tv_item = (TextView) view.findViewById(R.id.tv_item);

            iv_item.setImageResource(ids[position]);
            tv_item.setText(names[position]);
            return view;
        }

    }
}