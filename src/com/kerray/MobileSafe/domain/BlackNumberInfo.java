package com.kerray.MobileSafe.domain;


import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * 黑名单号码的业务bean
 */
@Table(name = "blacknumber", execAfterTableCreated = "create UNIQUE INDEX index_name ON blacknumber(number,mode)")
public class BlackNumberInfo extends EntityBase
{
    @Column(column = "number")          // 建议加上注解， 混淆后列名不受影响
    private String number;

    @Column(column = "mode")
    private String mode;

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    @Override
    public String toString()
    {
        return "BlackNumberInfo [number=" + number + ", mode=" + mode + "]";
    }

}
