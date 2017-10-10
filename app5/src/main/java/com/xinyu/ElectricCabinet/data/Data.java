package com.xinyu.ElectricCabinet.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hasee on 2017/3/9.
 */
public class Data {
    private SQLiteDatabase db;

    public Data(Context context) {
        File f = context.getDatabasePath("Battery.db").getParentFile();
        if (f.exists() == false) f.mkdirs();//注意是mkdirs()有个s 这样可以创建多重目录。
        String FullPath = f.getPath() + "/Battery.db";

        db = SQLiteDatabase.openOrCreateDatabase(FullPath, null);
        createTable();
    }

    private void createTable() {
        String stu_table = "create table if not exists battery_table(_id integer primary key autoincrement,battery_id integer,local integer,dianliang integer,dianya integer,dianliu integer,wendu integer,state varchar(20))";
        db.execSQL(stu_table);
    }
}
