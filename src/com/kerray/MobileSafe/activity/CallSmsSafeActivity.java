package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.db.dao.BlackNumberDao;
import com.kerray.MobileSafe.domain.BlackNumberInfo;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.List;

/**
 * @Created by kerray on 2015/3/28.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/28
 */
public class CallSmsSafeActivity extends Activity
{
    @ViewInject(R.id.lv_callsms_safe)
    private ListView lv_callsms_safe;

    @ViewInject(R.id.ll_loading)
    private LinearLayout ll_loading;

    private EditText et_blacknumber;
    private CheckBox cb_phone;
    private CheckBox cb_sms;
    private Button bt_ok;
    private Button bt_cancel;

    private List<BlackNumberInfo> infos;
    private BlackNumberDao dao;

    private CallSmsSafeAdapter mCallSmsSafeAdapter;
    private MyOnScrollListener mMyOnScrollListener;
    private DbUtils db;

    private int offset = 0;                 // 每页开始的位置
    private int maxnumber = 20;             // 每页显示 Item 数目


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_sms_safe);

        ViewUtils.inject(this);
        dao = new BlackNumberDao(this);
        mMyOnScrollListener = new MyOnScrollListener();

        fillData();
        // listview注册一个滚动事件的监听器。
        lv_callsms_safe.setOnScrollListener(mMyOnScrollListener);
    }

    private class MyOnScrollListener implements AbsListView.OnScrollListener
    {
        // 当滚动的状态发生变化的时候。
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            switch (scrollState)
            {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:                // 空闲状态
                // 判断当前listview滚动的位置
                // 获取最后一个可见条目在集合里面的位置。
                int lastposition = lv_callsms_safe.getLastVisiblePosition();

                // 集合里面有20个item 位置从0开始的 最后一个条目的位置 19
                if (lastposition == (infos.size() - 1))
                {
                    // Log.i("kerray", "列表被移动到了最后一个位置，加载更多的数据。。。");
                    offset += maxnumber;
                    fillData();
                }
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:        // 手指触摸滚动
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:               // 惯性滑行状态
                break;
            }
        }

        // 滚动的时候调用的方法。
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
        }
    }

    private void fillData()
    {
        ll_loading.setVisibility(View.VISIBLE);
        new Thread()
        {
            public void run()
            {
                if (infos == null)
                    infos = dao.findPart(offset, maxnumber);
                else
                    infos.addAll(dao.findPart(offset, maxnumber));              // 原来已经加载过数据了。

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ll_loading.setVisibility(View.INVISIBLE);
                        if (mCallSmsSafeAdapter == null)
                        {
                            mCallSmsSafeAdapter = new CallSmsSafeAdapter();
                            lv_callsms_safe.setAdapter(mCallSmsSafeAdapter);
                        } else
                            mCallSmsSafeAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }


    private class CallSmsSafeAdapter extends BaseAdapter
    {
        public int getCount()
        {
            return infos.size();
        }

        // 有多少个条目被显示，这个方法就会被调用多少次
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View view;
            ViewHolder holder;
            //1.减少内存中view对象创建的个数
            if (convertView == null)
            {
                //Log.i("kerray", "创建新的view对象：" + position);
                //把一个布局文件转化成  view对象。
                view = View.inflate(getApplicationContext(), R.layout.list_item_callsms, null);
                //2.减少子孩子查询的次数  内存中对象的地址。
                holder = new ViewHolder();
                ViewUtils.inject(holder, view);
                //当孩子生出来的时候找到他们的引用，存放在记事本，放在父亲的口袋
                view.setTag(holder);
            } else
            {
                //Log.i("kerray", "厨房有历史的view对象，复用历史缓存的view对象：" + position);
                view = convertView;
                holder = (ViewHolder) view.getTag();//5%
            }
            holder.tv_number.setText(infos.get(position).getNumber());
            String mode = infos.get(position).getMode();
            if ("1".equals(mode))
                holder.tv_mode.setText("电话拦截");
            else if ("2".equals(mode))
                holder.tv_mode.setText("短信拦截");
            else
                holder.tv_mode.setText("全部拦截");

            holder.iv_delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CallSmsSafeActivity.this);
                    builder.setTitle("警告");
                    builder.setMessage("确定要删除这条记录么？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //删除数据库的内容
                            try
                            {
                                dao.delete(infos.get(position).getNumber());
                            } catch (DbException e)
                            {
                                e.printStackTrace();
                            }
                            //更新界面。
                            infos.remove(position);
                            //通知listview数据适配器更新
                            mCallSmsSafeAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });
            return view;
        }

        public Object getItem(int position)
        {
            return null;
        }

        public long getItemId(int position)
        {
            return 0;
        }
    }

    /**
     * view对象的容器
     * 记录孩子的内存地址。
     * 相当于一个记事本
     */
    static class ViewHolder
    {
        @ViewInject(R.id.tv_black_number)
        TextView tv_number;
        @ViewInject(R.id.tv_block_mode)
        TextView tv_mode;
        @ViewInject(R.id.iv_delete)
        ImageView iv_delete;
    }

    @OnClick(R.id.addBlackNumber)
    public void addBlackNumber(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View contentView = View.inflate(this, R.layout.dialog_add_blacknumber, null);

        et_blacknumber = (EditText) contentView.findViewById(R.id.et_blacknumber);
        cb_phone = (CheckBox) contentView.findViewById(R.id.cb_phone);
        cb_sms = (CheckBox) contentView.findViewById(R.id.cb_sms);
        bt_cancel = (Button) contentView.findViewById(R.id.cancel);
        bt_ok = (Button) contentView.findViewById(R.id.ok);

        dialog.setView(contentView, 0, 0, 0, 0);
        dialog.show();

        bt_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        bt_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String blacknumber = et_blacknumber.getText().toString().trim();
                if (TextUtils.isEmpty(blacknumber))
                {
                    Toast.makeText(getApplicationContext(), "黑名单号码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                String mode;
                if (cb_phone.isChecked() && cb_sms.isChecked())
                    mode = "3";                 //全部拦截
                else if (cb_phone.isChecked())
                    mode = "1";                 //电话拦截
                else if (cb_sms.isChecked())
                    mode = "2";                 //短信拦截
                else
                {
                    Toast.makeText(getApplicationContext(), "请选择拦截模式", Toast.LENGTH_LONG).show();
                    return;
                }
                //数据被加到数据库
                dao.add(blacknumber, mode);
                //更新listview集合里面的内容。
                BlackNumberInfo info = new BlackNumberInfo();
                info.setMode(mode);
                info.setNumber(blacknumber);
                infos.add(0, info);
                //通知listview数据适配器数据更新了。
                mCallSmsSafeAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }
}