package com.kerray.MobileSafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.kerray.MobileSafe.domain.BlackNumberInfo;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 黑名单数据库的增删改查业务类
 */
public class BlackNumberDao
{
    private DbUtils db;

    public BlackNumberDao(Context pContext)
    {
        db = DbUtils.create(pContext, "blacknumber.db");
        db.configAllowTransaction(true);
        db.configDebug(true);

        addData();

    }

    /**
     * 添加数据
     */
    public boolean addData()
    {
        List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
        BlackNumberInfo mBlackNumberInfo;
        long basenumber = 13500000000l;
        Random random = new Random();
        try
        {
            for (int i = 0; i < 100; i++)
            {
                mBlackNumberInfo = new BlackNumberInfo();
                mBlackNumberInfo.setNumber(String.valueOf(basenumber + i));
                mBlackNumberInfo.setMode(String.valueOf(random.nextInt(3) + 1));
                result.add(mBlackNumberInfo);
            }
            db.saveAll(result);
            return true;
        } catch (DbException e)
        {
            e.printStackTrace();
        }
        return false;
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
        try
        {
            List<DbModel> bnLists = db.findDbModelAll(Selector.from(BlackNumberInfo.class).select("mode").where("number", "=", number));
            for (DbModel dd : bnLists)
                result = dd.getString("mode");
            //Log.i("kerray", "result====" + result);
        } catch (DbException e)
        {
            e.printStackTrace();
        }
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
            result = db.findAll(Selector.from(BlackNumberInfo.class).orderBy("id", true));
        } catch (DbException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询部分的黑名单号码
     * @param offset    从哪个位置开始获取数据
     * @param maxnumber 一次最多获取多少条记录
     * @return
     */
    public List<BlackNumberInfo> findPart(int offset, int maxnumber)
    {
        List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
        try
        {
            Thread.sleep(500);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        /*SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select number,mode from blacknumber order by _id desc  limit ? offset ? ",
          new String[] { String.valueOf(maxnumber), String.valueOf(offset) });
        while (cursor.moveToNext())
        {
            BlackNumberInfo info = new BlackNumberInfo();
            String number = cursor.getString(0);
            String mode = cursor.getString(1);
            info.setMode(mode);
            info.setNumber(number);
            result.add(info);
        }
        cursor.close();
        db.close();*/
        List<DbModel> bnLists = null;
        try
        {
            bnLists = db.findDbModelAll(Selector.from(BlackNumberInfo.class).select("number", "mode").orderBy("id", true).limit(maxnumber).offset(offset));
        } catch (DbException e)
        {
            e.printStackTrace();
        }
        for (DbModel dd : bnLists)
        {
            BlackNumberInfo info = new BlackNumberInfo();
            String number = dd.getString("number");
            String mode = dd.getString("mode");
            info.setNumber(number);
            info.setMode(mode);
            result.add(info);
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
        BlackNumberInfo mBlackNumberInfo = new BlackNumberInfo();
        mBlackNumberInfo.setNumber(number);
        mBlackNumberInfo.setMode(mode);
        try
        {
            db.saveBindingId(mBlackNumberInfo);
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
        db.delete(BlackNumberInfo.class, WhereBuilder.b("number", "=", number));
    }
}
