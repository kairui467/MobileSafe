package com.kerray.MobileSafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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
    private GridView list_home;
    private MyAdapter adapter;
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
        adapter = new MyAdapter();
        list_home.setAdapter(adapter);

        list_home.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                case 8://进入设置中心
                    Intent i = new Intent(HomeActivity.this, SettingActivity.class);
                    startActivity(i);
                    break;
                default:
                    break;
                }
            }
        });
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