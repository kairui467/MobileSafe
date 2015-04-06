package com.kerray.MobileSafe.utils;

import android.app.Activity;
import android.widget.Toast;

public class UIUtils {
	/**
	 * 显示吐司
	 * @param context 上下文
	 * @param msg	提示的内容
	 */
	public static void showToast(final Activity context,final String msg){
		if("main".equals(Thread.currentThread().getName())){
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}else{
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
