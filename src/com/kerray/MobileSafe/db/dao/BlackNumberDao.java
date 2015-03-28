package com.kerray.MobileSafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.kerray.MobileSafe.domain.BlackNumberInfo;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单数据库的增删改查业务类
 */
public class BlackNumberDao
{
    private DbUtils db;

    public BlackNumberDao(Context pContext)
    {
        db = DbUtils.create(pContext);
        db.configAllowTransaction(true);
        db.configDebug(true);
    }

    /**
     * 查询黑名单号码是是否存在
     * @param number
     * @return
     */
    public boolean find(String number) throws DbException
    {
        boolean result = false;
        /*SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from blacknumber where number=?", new String[]{number});
        if(cursor.moveToNext()){
            result = true;
        }
        cursor.close();
        db.close();*/
        Cursor mcursor = db.findFirst(Selector.from(BlackNumberInfo.class).where("number", null, new String[] { number }));
        if (mcursor.moveToNext())
            result = true;

        return result;
    }

    /**
     * 查询黑名单号码的拦截模式
     * @param number
     * @return 返回号码的拦截模式，不是黑名单号码返回null
     */
    public String findMode(String number)
    {
        String result = null;
        /*SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select mode from blacknumber where number=?", new String[]{number});
        if(cursor.moveToNext()){
            result = cursor.getString(0);
        }
        cursor.close();
        db.close();*/
        return result;
    }

    /**
     * 查询全部黑名单号码
     * @return
     */
    public List<BlackNumberInfo> findAll()
    {
        List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
        /*SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select number,mode from blacknumber order by _id desc", null);
        while(cursor.moveToNext()){
            BlackNumberInfo info = new BlackNumberInfo();
            String number = cursor.getString(0);
            String mode = cursor.getString(1);
            info.setMode(mode);
            info.setNumber(number);
            result.add(info);
        }
        cursor.close();
        db.close();*/
        try
        {
            result = db.findAll(Selector.from(BlackNumberInfo.class).orderBy("id"));
        } catch (DbException e)
        {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 添加黑名单号码
     * @param number 黑名单号码
     * @param mode   拦截模式 1.电话拦截 2.短信拦截 3.全部拦截
     */
    public void add(String number, String mode)
    {
        /*SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number", number);
        values.put("mode", mode);
        db.insert("blacknumber", null, values);
        db.close();*/
        ContentValues values = new ContentValues();
        values.put("number", number);
        values.put("mode", mode);
        try
        {
            db.saveBindingId(values);
        } catch (DbException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 修改黑名单号码的拦截模式
     * @param number  要修改的黑名单号码
     * @param newmode 新的拦截模式
     */
    public void update(String number, String newmode) throws DbException
    {
        /*SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mode", newmode);
        db.update("blacknumber", values, "number=?", new String[]{number});
        db.close();*/
        ContentValues values = new ContentValues();
        values.put("mode", newmode);
        db.update(values.getClass(), WhereBuilder.b("number", "=", number));
    }

    /**
     * 删除黑名单号码
     * @param number 要删除的黑名单号码
     */
    public void delete(String number) throws DbException
    {
        /*SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("blacknumber",  "number=?", new String[]{number});
        db.close();*/
        db.delete(BlackNumberInfo.class, WhereBuilder.b("number", "=", number));
    }
}
