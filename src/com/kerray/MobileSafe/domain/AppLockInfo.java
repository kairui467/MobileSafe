package com.kerray.MobileSafe.domain;


import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "applock", execAfterTableCreated = "create UNIQUE INDEX index_name ON applock(packname)")
public class AppLockInfo extends EntityBase
{
    @Column(column = "packname")
    private String packname;

    public String getPackname()
    {
        return packname;
    }

    public void setPackname(String packname)
    {
        this.packname = packname;
    }
}
