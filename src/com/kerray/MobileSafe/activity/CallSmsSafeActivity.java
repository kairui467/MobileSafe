package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        db = DbUtils.create(this);
        db.configAllowTransaction(true);
        db.configDebug(true);
        dao = new BlackNumberDao(this);

        addData();

        infos = dao.findAll();
        adapter = new CallSmsSafeAdapter();
        lv_callsms_safe.setAdapter(adapter);
    }

    private void addData()
    {
        List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
        BlackNumberInfo mBlackNumberInfo;
        long basenumber = 13500000000l;
        Random random = new Random();
        try
        {
            for (int i = 0; i < 20; i++)
            {
                mBlackNumberInfo = new BlackNumberInfo();
                mBlackNumberInfo.setNumber(String.valueOf(basenumber + i));
                mBlackNumberInfo.setMode(String.valueOf(random.nextInt(3) + 1));
                result.add(mBlackNumberInfo);
            }
            db.saveAll(result);
            Toast.makeText(this, "插入成功", Toast.LENGTH_SHORT).show();
        } catch (DbException e)
        {
            e.printStackTrace();
        }
    }

    private class CallSmsSafeAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return infos.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            ViewHolder holder;
            //1.减少内存中view对象创建的个数
            if (convertView == null)
            {
                Log.i("kerray", "创建新的view对象：" + position);
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
                Log.i("kerray", "厨房有历史的view对象，复用历史缓存的view对象：" + position);
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

            /*holder.iv_delete.setOnClickListener(new View.OnClickListener()
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
                            dao.delete(infos.get(position).getNumber());
                            //更新界面。
                            infos.remove(position);
                            //通知listview数据适配器更新
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });*/
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

}