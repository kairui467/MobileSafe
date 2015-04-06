package com.kerray.MobileSafe.recriver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import com.kerray.MobileSafe.service.UpdateWidgetService;

/**
 * 特殊的广播接受者
 * 1.写一个类 继承AppWidgetProvider
 */
public class MyWidget extends AppWidgetProvider
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, UpdateWidgetService.class);
        context.startService(i);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 第一次被创建时调用
     * @param context
     */
    public void onEnabled(Context context)
    {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.startService(intent);
        super.onEnabled(context);
    }

    /**
     * 移除完时被调用
     * @param context
     */
    public void onDisabled(Context context)
    {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }


}