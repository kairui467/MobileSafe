package com.kerray.MobileSafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import com.kerray.MobileSafe.R;

/**
 * @Created by kerray on 2015/3/23.
 * @方法名:com.kerray.MobileSafe.activity
 * @功能:
 * @参数:
 * @创建人:kerray
 * @创建时间:2015/3/23
 */
public class NumberAddressQueryActivity extends Activity
{

    private EditText ed_phone;
    private TextView result;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_addres_query);

        ed_phone = (EditText) findViewById(R.id.ed_phone);
        result = (TextView) findViewById(R.id.result);

        ed_phone.addTextChangedListener(new TextWatcher()
        {
            /**
             * 当文本发生变化之前回调
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            /**
             * 当文本发生变化的时候回调
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            /**
             * 当文本发生变化之后回调
             */
            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }
}