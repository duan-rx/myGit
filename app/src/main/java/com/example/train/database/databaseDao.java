package com.example.train.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.util.Log;

public class databaseDao {
    private final String TAG = "databaseDAO";
    private final databaseHelper databaseHelper;

    public databaseDao(Context context){
        //创建数据库
        databaseHelper = new databaseHelper(context);
        Log.d(TAG,"数据库创建成功");

    }

    public void insert(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String sql="insert into test(name,age) values('duan',20);";
        // db.execSQL(sql,new Object[]{1,"duan",20});
        db.execSQL(sql);
/*
        ContentValues values = new ContentValues();
        values.put("id",20);
        values.put("name","duan");
        values.put("age","20");
        values.put("phone","135");
        db.insert("test",null,values);

*/

        db.close();
        Log.d(TAG,"数据插入成功");

    }
    public boolean insert(String sql){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            return true;
        }catch (Exception e){
            Log.i("TAG","insert"+e.getMessage());
            db.endTransaction();
            db.close();
            return false;
        }
        finally {

        }


    }
    public void delete(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String sql = "delete from test where age=20 or age =30 ";
        db.execSQL(sql);

        db.close();
        Log.d(TAG,"数据删除成功");
    }
    public void delete(String sql){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }catch (Exception e){

        }
        finally {
            db.endTransaction();
        }
        db.close();
    }
    public void update(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        //String sql = "update test set age = '30' where age = 20";
        //db.execSQL(sql);
        ContentValues values = new ContentValues();
        values.put("age",30);
        values.put("name","helllo");
        db.update("test",values,null,null);
        db.close();
        Log.d(TAG,"数据更新成功");
    }
    public boolean update(String sql){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            return true;
        }catch (Exception e){
            db.endTransaction();
            Log.i("TAG","update"+e.getMessage());
            db.close();
            return false;
        }
    }
    public String query(){
        String age="";
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql = "select * from test";
        Cursor cursor = db.rawQuery(sql,null);
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex("id");
            String id = cursor.getString(index);
            Log.d(TAG,"name = " + id);
            index= cursor.getColumnIndex("name");
            String name = cursor.getString(index);
            Log.d(TAG,"name = " + name);

            index = cursor.getColumnIndex("age");
            age = cursor.getString(index);
            Log.d(TAG,"age ="+ age);
        }
        cursor.close();
        db.close();
        return  age;
    }
    public Cursor query(String sql){
        Cursor cursor = null ;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            cursor = db.rawQuery(sql,null);
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.i("TAG","querryError:"+e.getMessage());
            return null;
        }finally {
            db.endTransaction();
        }

        return cursor;
    }
}
