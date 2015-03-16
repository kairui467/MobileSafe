package com.kerray.MobileSafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kerray.MobileSafe.R;

/**
 * @Created by kerray on 2015/3/16.
 * @方法名:com.kerray.MobileSafe.ui
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/16
 */
public class SettingItemView extends RelativeLayout
{
    private CheckBox cb_status;
    private TextView tv_title;
    private TextView tv_desc;

    /**
     * 初始化布局文件
     * @param context
     */
    private void iniView(Context context)
    {
        //把一个布局文件---> View 并且加载在SettingItemView
        View.inflate(context, R.layout.setting_item_view, this);
        cb_status = (CheckBox) this.findViewById(R.id.cb_status);
        tv_desc = (TextView) this.findViewById(R.id.tv_desc);
        tv_title = (TextView) this.findViewById(R.id.tv_title);
    }

    public SettingItemView(Context context)
    {
        super(context);
        iniView(context);
    }

    public SettingItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        iniView(context);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        iniView(context);
    }

    /**
     * 校验组合控件是否选中
     */
    public boolean isChecked()
    {
        return cb_status.isChecked();
    }

    /**
     * 设置组合控件的状态
     */
    public void setChecked(boolean checked)
    {
        cb_status.setChecked(checked);
    }

    /**
     * 设置 组合控件的描述信息
     */
    public void setDesx(String text)
    {
        tv_desc.setText(text);
    }
}
