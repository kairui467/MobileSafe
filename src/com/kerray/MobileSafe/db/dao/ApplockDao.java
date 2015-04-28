package com.kerray.MobileSafe.db.dao;

import android.content.Context;
import android.content.Intent;
import com.kerray.MobileSafe.domain.AppLockInfo;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序锁的dao
 */
public class ApplockDao
{
    private DbUtils db;
    private Context mContext;

    /**
     * 构造方法
     * @param pContext 上下文
     */
    public ApplockDao(Context pContext)
    {
        mContext = pContext;
        db = DbUtils.create(pContext, "applock.db");
        db.configAllowTransaction(true);
        //db.configDebug(true);
    }

    /**
     * 添加一个要锁定应用程序的包名
     * @param pPackname
     */
    public void add(String pPackname)
    {
        AppLockInfo mAppLockInfo = new AppLockInfo();
        mAppLockInfo.setPackname(pPackname);
        try
        {
            db.saveBindingId(mAppLockInfo);

            Intent intent = new Intent();
            intent.setAction("com.kerray.MobileSafe.applockchange");
            mContext.sendBroadcast(intent);
            //context.getContentResolver().notifyChange(uri, observer);
        } catch (DbException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 删除一个要锁定应用程序的包名
     * @param packname
     */
    public void delete(String packname)
    {
        try
        {
            db.delete(AppLockInfo.class, WhereBuilder.b("packname", "=", packname));
            Intent intent = new Intent();
            intent.setAction("com.kerray.MobileSafe.applockchange");
            mContext.sendBroadcast(intent);
        } catch (DbException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 查询一条程序锁包名记录是否存在
     * @param packname
     * @return
     */
    public boolean find(String packname)
    {
        boolean result = false;
        try
        {
            long size = db.count(Selector.from(AppLockInfo.class).where("packname", "=", packname));
            if (size != 0)
                result = true;
        } catch (DbException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询所有程序锁包名记录
     * @return
     */
    public List<String> findAll()
    {
        List<String> protectPacknames = new ArrayList<String>();
        try
        {
            List<AppLockInfo> bnLists = db.findAll(Selector.from(AppLockInfo.class));
            for (AppLockInfo a : bnLists)
                protectPacknames.add(a.getPackname());
        } catch (DbException e)
        {
            e.printStackTrace();
        }
        return protectPacknames;
    }
}
