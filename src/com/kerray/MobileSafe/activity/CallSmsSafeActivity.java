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

    private EditText et_blacknumber;
    private CheckBox cb_phone;
    private CheckBox cb_sms;
    private Button bt_ok;
    private Button bt_cancel;

    private List<BlackNumberInfo> infos;
    private BlackNumberDao dao;

    private CallSmsSafeAdapter adapter;
    private DbUtils db;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_sms_safe);

        ViewUtils.inject(this);

        dao = new BlackNumberDao(this);

        infos = dao.findAll();
        adapter = new CallSmsSafeAdapter();
        lv_callsms_safe.setAdapter(adapter);
    }


    private class CallSmsSafeAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return infos.size();
        }

        @Override
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
                holder.tv_number = (TextView) view.findViewById(R.id.tv_black_number);
                holder.tv_mode = (TextView) view.findViewById(R.id.tv_block_mode);
                holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
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
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });
            return view;
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
    }

    /**
     * view对象的容器
     * 记录孩子的内存地址。
     * 相当于一个记事本
     */
    static class ViewHolder
    {
        TextView tv_number;
        TextView tv_mode;
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
                    //全部拦截
                    mode = "3";
                else if (cb_phone.isChecked())
                    //电话拦截
                    mode = "1";
                else if (cb_sms.isChecked())
                    //短信拦截
                    mode = "2";
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
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

}