package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.domain.TaskInfo;
import com.kerray.MobileSafe.engine.TaskInfoParser;
import com.kerray.MobileSafe.utils.SystemInfoUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

public class TaskManagerActivity extends Activity
{
    @ViewInject(R.id.tv_running_prcesscount)
    private TextView tv_running_prcesscount;
    @ViewInject(R.id.tv_ram_info)
    private TextView tv_ram_info;
    @ViewInject(R.id.lv_taskmanger)
    private ListView lv_taskmanger;
    @ViewInject(R.id.ll_loading)
    private LinearLayout ll_loading;

    private List<TaskInfo> infos;               // 所有进程信息的集合
    private List<TaskInfo> userTaskInfos;
    private List<TaskInfo> systemTaskInfos;

    private int runningProcessCount;            // 正在运行的进程数量

    private long totalAvailMem;                 // 总的可用内存

    private TaskManagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager);
        ViewUtils.inject(this);

        // 设置内存空间的大小 和 正在运行的进程的数量
        totalAvailMem = SystemInfoUtils.getAvailMem(this);
        long totalMem = SystemInfoUtils.getTotalMem();

        runningProcessCount = SystemInfoUtils.getRunningPocessCount(this);

        tv_running_prcesscount.setText("运行中进程：" + runningProcessCount + "个");
        tv_ram_info.setText("可用/总内存：" +
          Formatter.formatFileSize(this, totalAvailMem) + "/"
          + Formatter.formatFileSize(this, totalMem));

        fillData();

        lv_taskmanger.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object obj = lv_taskmanger.getItemAtPosition(position);
                if (obj != null && obj instanceof TaskInfo)
                {
                    TaskInfo info = (TaskInfo) obj;
                    if (info.getPackname().equals(getPackageName()))
                        return;                                           //就是我自己

                    ViewHolder holder = (ViewHolder) view.getTag();
                    if (info.isChecked())
                    {
                        holder.cb_status.setChecked(false);
                        info.setChecked(false);
                    } else
                    {
                        holder.cb_status.setChecked(true);
                        info.setChecked(true);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        if (adapter != null)
            adapter.notifyDataSetChanged();         // 通知界面更新
        super.onStart();
    }

    private void fillData()
    {
        ll_loading.setVisibility(View.VISIBLE);
        new Thread()
        {
            @Override
            public void run()
            {
                infos = TaskInfoParser.getRunningTaskInfos(TaskManagerActivity.this);

                userTaskInfos = new ArrayList<TaskInfo>();
                systemTaskInfos = new ArrayList<TaskInfo>();
                for (TaskInfo info : infos)
                {
                    if (info.isUsertask())
                        userTaskInfos.add(info);
                    else
                        systemTaskInfos.add(info);
                }
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ll_loading.setVisibility(View.INVISIBLE);
                        adapter = new TaskManagerAdapter();
                        lv_taskmanger.setAdapter(adapter);
                    }
                });
            }
        }.start();

    }

    static class ViewHolder
    {
        @ViewInject(R.id.iv_task_icon)
        ImageView iv_icon;
        @ViewInject(R.id.tv_task_name)
        TextView tv_name;
        @ViewInject(R.id.tv_task_size)
        TextView tv_size;
        @ViewInject(R.id.cb_task_status)
        CheckBox cb_status;
    }

    private class TaskManagerAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
            boolean showsystem = sp.getBoolean("showsystem", true);
            if (showsystem)
                return userTaskInfos.size() + 1 + systemTaskInfos.size() + 1;
            else
                return userTaskInfos.size() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final TaskInfo info;
            if (position == 0)
            {
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.WHITE);
                tv.setText("用户进程：" + userTaskInfos.size() + "个");
                return tv;
            } else if (position == (userTaskInfos.size() + 1))
            {
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.WHITE);
                tv.setText("系统进程：" + systemTaskInfos.size() + "个");
                return tv;
            } else if (position <= userTaskInfos.size())
                info = userTaskInfos.get(position - 1);
            else
                info = systemTaskInfos.get(position - 1 - userTaskInfos.size() - 1);

            View view;
            ViewHolder holder;
            if (convertView != null && convertView instanceof RelativeLayout)
            {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else
            {
                view = View.inflate(getApplicationContext(), R.layout.list_item_task_manager, null);
                holder = new ViewHolder();
                ViewUtils.inject(holder, view);
                view.setTag(holder);
            }

            holder.iv_icon.setImageDrawable(info.getIcon());
            holder.tv_name.setText(info.getAppname());
            holder.tv_size.setText("占用内存：" + Formatter.formatFileSize(getApplicationContext(), info.getMemsize()));
            holder.cb_status.setChecked(info.isChecked());

            if (info.getPackname().equals(getPackageName()))
                holder.cb_status.setVisibility(View.INVISIBLE);                 // 就是我自己
            else
                holder.cb_status.setVisibility(View.VISIBLE);
            return view;
        }

        @Override
        public Object getItem(int position)
        {
            TaskInfo info;
            if (position == 0)
                return null;
            else if (position == (userTaskInfos.size() + 1))
                return null;
            else if (position <= userTaskInfos.size())
                info = userTaskInfos.get(position - 1);
            else
                info = systemTaskInfos.get(position - 1 - userTaskInfos.size() - 1);

            return info;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }
    }

    /**
     * 选择全部的item
     * @param view
     */
    public void selectAll(View view)
    {
        for (TaskInfo info : userTaskInfos)
        {
            if (info.getPackname().equals(getPackageName()))
                continue;
            info.setChecked(true);
        }
        for (TaskInfo info : systemTaskInfos)
            info.setChecked(true);

        //通知界面更新
        adapter.notifyDataSetChanged();
    }

    /**
     * 反选全部的item
     * @param view
     */
    public void selectOpposite(View view)
    {
        for (TaskInfo info : userTaskInfos)
        {
            if (info.getPackname().equals(getPackageName()))
                continue;
            info.setChecked(!info.isChecked());
        }
        for (TaskInfo info : systemTaskInfos)
            info.setChecked(!info.isChecked());
        // 通知界面更新
        adapter.notifyDataSetChanged();
    }

    /**
     * 杀死后台进程
     * @param view
     */
    public void killProcess(View view)
    {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int count = 0;
        long savemem = 0;
        List<TaskInfo> killedTaskInfos = new ArrayList<TaskInfo>();

        // 在遍历集合的时候，不可以修改集合的大小
        for (TaskInfo info : userTaskInfos)
        {
            if (info.isChecked())
            {
                count++;
                savemem += info.getMemsize();
                am.killBackgroundProcesses(info.getPackname());
                killedTaskInfos.add(info);
            }
        }

        for (TaskInfo info : systemTaskInfos)
        {
            if (info.isChecked())
            {
                count++;
                savemem += info.getMemsize();
                am.killBackgroundProcesses(info.getPackname());
                killedTaskInfos.add(info);
            }
        }
        for (TaskInfo info : killedTaskInfos)
        {
            if (info.isUsertask())
                userTaskInfos.remove(info);
            else
                systemTaskInfos.remove(info);
        }

        runningProcessCount -= count;
        totalAvailMem += savemem;

        // 更新标题
        tv_running_prcesscount.setText("运行中进程：" + runningProcessCount + "个");
        tv_ram_info.setText("可用/总内存：" + Formatter.formatFileSize(this, totalAvailMem) + "/"
          + Formatter.formatFileSize(this,
          SystemInfoUtils.getTotalMem()));
        Toast.makeText(this, "杀死了" + count + "个进程,释放了"
          + Formatter.formatFileSize(this, savemem) + "的内存", Toast.LENGTH_LONG).show();
        // 刷新界面
        // 老实 fillData();
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置按钮
     * @param view
     */
    public void openSetting(View view)
    {
        Intent intent = new Intent(this, TaskManagerSettingActivity.class);
        startActivity(intent);
    }

}