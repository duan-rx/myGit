package com.example.train.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.train.constants.database;

public class databaseHelper extends SQLiteOpenHelper {
    private static final String Tag ="DatabaseHelper";
    //public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
    public databaseHelper(@Nullable Context context) {

        super(context, database.DATABASE_NAME, null, database.VERSION_CODE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建时的回调
        //第一次会被调用
        Log.d(Tag,"创建数据库");
        String sql=" create table user(\n" +
                "                userId text primary key ,\n" +
                "                userName text not null,\n" +
                "                userPhone text not null,\n" +
                "                userPassword text not null,\n" +
                "                userPower int not null,\n" +//0  普通用户 1 管理员
                "                userStatus int not null default 1\n" +//0 为删除用户，1使用账号，2账号申请
                "        );";
        db.execSQL(sql);
        sql=" create table instrument(\n" +
                "                inCode text primary key ,\n" +
                "                inName text not null,\n" +
                "                bleAddress text,\n" +
                "                status int not null default 1\n" + //0 为删除
                "        );";
        db.execSQL(sql);
        sql=" create table instrumentItem(\n" +
                "                inCode text not null,\n" +
                "                itemCode text primary key ,\n" +
                "                itemName text not null,\n" +
                "                itemResultType int default 0,\n" +//1 代表数字类型，2 字符类型，3 布尔类型
                "                channelCode text ,\n" +
                "                itemUnit text default '无',\n" +
                "                itemReference text default '无',\n" +
                "                itemMax double ,\n" +
                "                itemMin double,\n" +
                "                lineColor int default -16777216\n" +
                "        );";
        db.execSQL(sql);
        sql=" create table histroy(\n" +
                "                userId text not null,\n" +
                "                count int not null,\n" +
                "                startTime timestamp not null default current_timestamp,\n"+
                "                stopTime timestamp not null default current_timestamp\n"+
                "        );";
        db.execSQL(sql);
        sql=" create table result(\n" +
                "                userId text not null,\n" +
                "                inCode text not null,\n" +
                "                itemCode text not null,\n" +
                "                itemResultNum double,\n" +
                "                itemResultString text,\n" +
                "                itemResultBlob text,\n" +
                "                count int default -1,\n" +
                "                status int default 0,\n" +  // 0 为正常数据，1为测试数据
                "                dataTime timestamp not null default current_timestamp\n" +
                "        );";
        db.execSQL(sql);
        sql=" create table bleUUID(\n" +
                "                serverUUID text not null default '0000ffe0-0000-1000-8000-00805f9b34fb',\n" +
                "                charUUID text not null default '0000ffe1-0000-1000-8000-00805f9b34fb' \n" +
                "        );";
        db.execSQL(sql);
        sql=" create table originalData(\n" +
                "                data text not null,\n" +
                "                startChar text default '{' ,\n" +
                "                lastChar text  default '}',\n" +
                "                splitChar text default ':'\n" +
                "        );";
        db.execSQL(sql);
   }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级时的回调
        Log.d(Tag,"升级数据库");

        switch (oldVersion){
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }
}
