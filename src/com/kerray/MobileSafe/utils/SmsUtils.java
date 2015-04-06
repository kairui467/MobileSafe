package com.kerray.MobileSafe.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 短信的工具类
 */
public class SmsUtils
{
    /**
     * 备份短信的回调接口
     */
    public interface BackUpCallBack
    {
        /**
         * 开始备份的时候，设置进度的最大值
         * @param max 总进度
         */
        public void beforeBackup(int max);

        /**
         * 备份过程中，增加进度
         * @param progress 当前进度
         */
        public void onSmsBackup(int progress);
    }

    /**
     * @param pContext        上下文
     * @param pBackUpCallBack 备份短信的接口
     * @throws Exception
     */
    public static void backupSms(Context pContext, BackUpCallBack pBackUpCallBack) throws Exception
    {
        ContentResolver mContentResolver = pContext.getContentResolver();
        File mFile = new File(Environment.getExternalStorageDirectory(), "backup.xml");
        FileOutputStream mFileOutputStream = new FileOutputStream(mFile);
        // 把用户的短信一条一条读出来，按照一定的格式写到文件里
        XmlSerializer mXmlSerializer = Xml.newSerializer();                 // 获取xml文件的生成器（序列化器）
        // 初始化生成器
        mXmlSerializer.setOutput(mFileOutputStream, "utf-8");
        mXmlSerializer.startDocument("utf-8", true);
        mXmlSerializer.startTag(null, "smss");
        Uri mUri = Uri.parse("content://sms/");
        Cursor mCursor = mContentResolver.query(mUri, new String[] { "body", "address", "type", "date" },
          null, null, null);
        // 开始备份的时候，设置进度条的最大值
        int max = mCursor.getCount();
        pBackUpCallBack.beforeBackup(max);
        mXmlSerializer.attribute(null, "max", max + "");                    // 添加 max 标识的头
        int process = 0;
        /**
         * <smss>
         *     <msx />
         *     <sms>
         *         <body>内容</body>
         *         <address>电话号码</address>
         *         <type>短信类型</type>
         *         <date>短信时间</date>
         *     </sms>
         * </smss>
         */
        while (mCursor.moveToNext())
        {
            Thread.sleep(500);
            String body = mCursor.getString(0);
            String address = mCursor.getString(1);
            String type = mCursor.getString(2);
            String date = mCursor.getString(3);

            mXmlSerializer.startTag(null, "sms");

            mXmlSerializer.startTag(null, "body");
            mXmlSerializer.text(body);
            mXmlSerializer.endTag(null, "body");

            mXmlSerializer.startTag(null, "address");
            mXmlSerializer.text(address);
            mXmlSerializer.endTag(null, "address");

            mXmlSerializer.startTag(null, "type");
            mXmlSerializer.text(type);
            mXmlSerializer.endTag(null, "type");

            mXmlSerializer.startTag(null, "date");
            mXmlSerializer.text(date);
            mXmlSerializer.endTag(null, "date");

            mXmlSerializer.endTag(null, "sms");

            // 备份过程中，增加进度
            process++;
            pBackUpCallBack.onSmsBackup(process);
        }
        mCursor.close();
        mXmlSerializer.endTag(null, "smss");
        mXmlSerializer.endDocument();
        mFileOutputStream.close();
    }

    /**
     * 还原短信
     * @param pContext
     * @param pflag    是否清理原来的短信
     */
    public static void restoreSms(Context pContext, boolean pflag)
    {
        Uri uri = Uri.parse("content://sms/");
        if (pflag)
            pContext.getContentResolver().delete(uri, null, null);

        // 1.读取sd卡上的xml文件
        // Xml.newPullParser();

        // 2.读取max

        // 3.读取每一条短信信息，body date type address

        // 4.把短信插入到系统短息应用。

        ContentValues values = new ContentValues();
        values.put("body", "测试还原短信");
        values.put("date", "1395045035573");
        values.put("type", "1");
        values.put("address", "5558");
        pContext.getContentResolver().insert(uri, values);
    }
}
