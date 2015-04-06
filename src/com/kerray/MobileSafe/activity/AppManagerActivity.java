package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import com.kerray.MobileSafe.R;
import com.kerray.MobileSafe.domain.AppInfo;
import com.kerray.MobileSafe.engine.AppInfoProvider;
import com.kerray.MobileSafe.utils.DensityUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Created by kerray on 2015/4/1.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/4/1
 */
public class AppManagerActivity extends Activity implements View.OnClickListener
{
    @ViewInject(R.id.tv_avail_rom)
    private TextView tv_avail_rom;
    @ViewInject(R.id.tv_avail_sd)
    private TextView tv_avail_sd;
    @ViewInject(R.id.tv_status)
    private TextView tv_status;                 // 当前程序信息的状态

    @ViewInject(R.id.lv_app_manager)
    private ListView lv_app_manager;
    @ViewInject(R.id.ll_loading)
    private LinearLayout ll_loading;

    private List<AppInfo> appInfos;             // 所有的应用程序包信息
    private List<AppInfo> userAppInfos;         // 用户应用程序的集合
    private List<AppInfo> systemAppInfos;       // 系统应用程序的集合

    private PopupWindow popupWindow;            // 弹出悬浮窗体

    private LinearLayout ll_start;              // 开启
    private LinearLayout ll_share;              // 分享
    private LinearLayout ll_uninstall;          // 卸载

    private AppInfo appInfo;                    // 被点击的条目
    private AppManagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        ViewUtils.inject(this);

        long sdsize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
        long romsize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());

        tv_avail_sd.setText("SD卡可用空间：" + Formatter.formatFileSize(this, sdsize));
        tv_avail_rom.setText("内存可用空间：" + Formatter.formatFileSize(this, romsize));

        fillData();

        // 给listview注册一个滚动的监听器
        lv_app_manager.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            // 滚动的时候调用的方法。
            // firstVisibleItem 第一个可见条目在listview集合里面的位置。
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (userAppInfos != null && systemAppInfos != null)
                {
                    dismissPopupWindow();
                    if (firstVisibleItem > userAppInfos.size())
                        tv_status.setText("系统程序：" + systemAppInfos.size() + "个");
                    else
                        tv_status.setText("用户程序：" + userAppInfos.size() + "个");
                }
            }
        });

        // 设置listview的点击事件
        lv_app_manager.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == 0)
                    return;
                else if (position == (userAppInfos.size() + 1))
                    return;
                else if (position <= userAppInfos.size())     //用户程序
                {
                    int newposition = position - 1;
                    appInfo = userAppInfos.get(newposition);
                } else
                {
                    int newposition = position - 1 - userAppInfos.size() - 1;
                    appInfo = systemAppInfos.get(newposition);
                }

                View contentView = View.inflate(getApplicationContext(), R.layout.popup_app_item, null);

                ll_start = (LinearLayout) contentView.findViewById(R.id.ll_start);
                ll_share = (LinearLayout) contentView.findViewById(R.id.ll_share);
                ll_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_uninstall);

                ll_start.setOnClickListener(AppManagerActivity.this);
                ll_share.setOnClickListener(AppManagerActivity.this);
                ll_uninstall.setOnClickListener(AppManagerActivity.this);

                popupWindow = new PopupWindow(contentView, -2, -2);     // ViewGroup.LayoutParams.WRAP_CONTENT = -2
                // 动画效果的播放必须要求窗体有背景颜色。
                // 透明颜色也是颜色
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int[] location = new int[2];
                view.getLocationInWindow(location);
                // 在代码里面设置的宽高值 都是像素。---》dip
                int px = DensityUtil.dip2px(getApplicationContext(), 60);
                popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP, px, location[1]);
                ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0,
                  Animation.RELATIVE_TO_SELF, 0.5f);                    //  缩放动画
                sa.setDuration(300);
                AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);      // 透明度动画
                aa.setDuration(300);
                AnimationSet set = new AnimationSet(false);             // 动画集
                set.addAnimation(sa);
                set.addAnimation(aa);
                contentView.startAnimation(set);

            }
        });
    }

    @Override
    protected void onDestroy()
    {
        dismissPopupWindow();
        super.onDestroy();
    }

    private void fillData()
    {
        ll_loading.setVisibility(View.VISIBLE);
        new Thread()
        {
            public void run()
            {
                appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
                userAppInfos = new ArrayList<AppInfo>();
                systemAppInfos = new ArrayList<AppInfo>();
                for (AppInfo info : appInfos)
                    if (info.isUserApp())
                        userAppInfos.add(info);
                    else
                        systemAppInfos.add(info);
                // 加载listview的数据适配器
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (adapter == null)
                        {
                            adapter = new AppManagerAdapter();
                            lv_app_manager.setAdapter(adapter);
                        } else
                            adapter.notifyDataSetChanged();
                        ll_loading.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }.start();
    }

    /**
     * 布局对应的点击事件
     * @param v
     */
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.ll_start:
            //Log.i("kerray", "启动：" + appInfo.getName());
            startApplication();
            break;
        case R.id.ll_share:
            //Log.i("kerray", "分享：" + appInfo.getName());
            shareApplication();
            break;
        case R.id.ll_uninstall:
            //Log.i("kerray", "卸载：" + appInfo.getName());
            if (appInfo.isUserApp())
                uninstallAppliation();
            else
                Toast.makeText(this, "系统应用只有获取root权限才可以卸载", Toast.LENGTH_SHORT).show();
            //Runtime.getRuntime().exec("");
            break;
        }
    }

    /**
     * 开启一个应用程序
     */
    private void startApplication()
    {
        // 查询这个应用程序的入口activity。 把他开启起来。
        PackageManager pm = getPackageManager();
        // Intent intent = new Intent();
        // intent.setAction("android.intent.action.MAIN");
        // intent.addCategory("android.intent.category.LAUNCHER");
        // //查询出来了所有的手机上具有启动能力的activity。
        // List<ResolveInfo> infos = pm.queryIntentActivities(intent,
        // PackageManager.GET_INTENT_FILTERS);
        Intent intent = pm.getLaunchIntentForPackage(appInfo.getPackname());
        if (intent != null)
            startActivity(intent);
        else
            Toast.makeText(this, "不能启动当前应用", Toast.LENGTH_SHORT).show();
    }

    /**
     * 分享一个应用程序
     */
    private void shareApplication()
    {
        // Intent { act=android.intent.action.SEND typ=text/plain flg=0x3000000 cmp=com.android.mms/.ui.ComposeMessageActivity (has extras) }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "推荐您使用一款软件,名称叫：" + appInfo.getName());
        startActivity(intent);
    }

    /**
     * 卸载应用
     */
    private void uninstallAppliation()
    {
        // <action android:name="android.intent.action.VIEW" />
        // <action android:name="android.intent.action.DELETE" />
        // <category android:name="android.intent.category.DEFAULT" />
        // <data android:scheme="package" />
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + appInfo.getPackname()));
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        fillData();             // 刷新界面
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AppManagerAdapter extends BaseAdapter
    {
        // 控制listview有多少个条目
        public int getCount()
        {
            return userAppInfos.size() + 1 + systemAppInfos.size() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            AppInfo appInfo;
            if (position == 0)
            {       // 显示的是用程序有多少个的小标签
                TextView tv = new TextView(getApplicationContext());
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                tv.setText("用户程序：" + userAppInfos.size() + "个");
                return tv;
            } else if (position == (userAppInfos.size() + 1))
            {
                TextView tv = new TextView(getApplicationContext());
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                tv.setText("系统程序：" + systemAppInfos.size() + "个");
                return tv;
            } else if (position <= userAppInfos.size())         // 用户程序
            {
                int newposition = position - 1;                 // 因为多了一个textview的文本占用了位置
                appInfo = userAppInfos.get(newposition);
            } else                                              // 系统程序
            {
                int newposition = position - 1 - userAppInfos.size() - 1;
                appInfo = systemAppInfos.get(newposition);
            }

            View view;
            ViewHolder holder;
            if (convertView != null && convertView instanceof RelativeLayout)
            {   // 不仅需要检查是否为空，还要判断是否是合适的类型去复用
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else
            {
                view = View.inflate(getApplicationContext(), R.layout.list_item_appinfo, null);
                holder = new ViewHolder();
                ViewUtils.inject(holder, view);
                view.setTag(holder);
            }

            holder.iv_icon.setImageDrawable(appInfo.getIcon());
            holder.tv_name.setText(appInfo.getName());
            holder.tv_size.setText(Formatter.formatFileSize(getApplicationContext(), appInfo.getAppSize()));
            if (appInfo.isInRom())
                holder.tv_location.setText("手机内存");
            else
                holder.tv_location.setText("外部存储");
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

    static class ViewHolder
    {
        @ViewInject(R.id.tv_app_name)
        TextView tv_name;
        @ViewInject(R.id.tv_app_size)
        TextView tv_size;
        @ViewInject(R.id.tv_app_location)
        TextView tv_location;
        @ViewInject(R.id.iv_app_icon)
        ImageView iv_icon;
    }

    /**
     * 获取某个目录的可用空间
     * @param path
     * @return
     */
    private long getAvailSpace(String path)
    {
        StatFs statFs = new StatFs(path);
        statFs.getBlockCount();                     // 获取分区的个数
        long size = statFs.getBlockSize();          // 获取分区的大小
        long count = statFs.getAvailableBlocks();   // 获取可用的区块的个数
        return size * count;
    }

    private void dismissPopupWindow()
    {
        // 把旧的弹出窗体关闭掉。
        if (popupWindow != null && popupWindow.isShowing())
        {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }
}
