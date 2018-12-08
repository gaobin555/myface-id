package com.orbbec.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.orbbec.model.User;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author mac
 * @date 16/7/11
 */
public class DataSource {
    private SQLiteDatabase database;
    private MySqLiteHelper dbHelper;
    private String[] allColumns = {
            MySqLiteHelper.USER_ID,
            MySqLiteHelper.PERSON_ID,
            MySqLiteHelper.USER_NAME,
            MySqLiteHelper.USER_AGE,
            MySqLiteHelper.USER_GENDER,
            MySqLiteHelper.USER_SCORE,
            MySqLiteHelper.USER_BIRTH
    };

    public DataSource(Context context) {
        dbHelper = new MySqLiteHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public void insert(User user) {
        open();
        ContentValues values = new ContentValues();
        values.put(MySqLiteHelper.PERSON_ID, user.getPersonId());
        values.put(MySqLiteHelper.USER_NAME, user.getName());
        values.put(MySqLiteHelper.USER_AGE, user.getAge());
        values.put(MySqLiteHelper.USER_GENDER, user.getGender());
        values.put(MySqLiteHelper.USER_SCORE, user.getScore());
        values.put(MySqLiteHelper.USER_BIRTH, user.getBirdthday());
        database.insert(MySqLiteHelper.TABLE_USER, null, values);
        close();
    }


    public List<User> getAllUser() {
        open();
        database.beginTransaction();
        List<User> result = new ArrayList<>();
        Cursor cursor = database.query(MySqLiteHelper.TABLE_USER,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Log.d("lgp", "getAllUser: " + cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_BIRTH)) + ":" + cursor.getString(4));
            User user = new User();
            user.setUserId(cursor.getString(0));
            user.setPersonId(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setAge(cursor.getString(3));
            user.setBirdthday(cursor.getString(4));
            user.setScore(cursor.getString(5));
            result.add(user);
            cursor.moveToNext();
        }
        database.endTransaction();
        cursor.close();
        close();
        return result;
    }

    public User getUserByPersonId(String personId) {
        open();
        User user = new User();
        Cursor cursor = database.query(MySqLiteHelper.TABLE_USER,
                null,
                "personId = ?",
                new String[]{personId},
                null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            user.setUserId(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_ID)));
            user.setPersonId(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.PERSON_ID)));
            user.setName(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_NAME)));
            user.setAge(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_AGE)));
            user.setGender(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_GENDER)));
            user.setScore(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_SCORE)));

        } else {
            user = null;
        }
        close();
        return user;
    }

    public boolean checkUserByName(String name) {
        open();
        Cursor cursor = database.query(MySqLiteHelper.TABLE_USER,
                null,
                MySqLiteHelper.USER_NAME + " = ?",
                new String[]{name},
                null, null, null);
        Log.d("lgp", "checkUserByName: "+cursor.getCount());
        if (cursor.getCount() >= 1) {
            close();
            return true;
        } else {
            close();
            return false;
        }
    }


    public User getUserByName(String name) {
        open();

        Log.d("lgp", "getUserByName: name " + name);
        User user = new User();
        Cursor cursor = database.query(MySqLiteHelper.TABLE_USER,
                null,
                MySqLiteHelper.USER_NAME + " = ?",
                new String[]{name},
                null, null, null);
        Log.d("lgp", "getUserByName: " + cursor.getCount());
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            user.setUserId(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_ID)));
            user.setPersonId(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.PERSON_ID)));
            user.setName(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_NAME)));
            user.setBirdthday(cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_BIRTH)));
            Log.d("lgp", "getUserByName: USER_BIRTH" + cursor.getString(cursor.getColumnIndex(MySqLiteHelper.USER_BIRTH)));
        } else {
            user = null;
        }
        close();
        return user;
    }


    public void upDataByName(String newName, String oldName, String brithday) {
        int i = 0;
        open();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySqLiteHelper.USER_NAME, newName);
        contentValues.put(MySqLiteHelper.USER_BIRTH, brithday);
        i = database.update(MySqLiteHelper.TABLE_USER, contentValues, MySqLiteHelper.USER_NAME + "=?", new String[]{oldName});
        Log.d("lgp", "upDataByName: " + i);
    }

    public void deleteByName(String name) {
        open();
        Log.d("lgp", "deleteByName: "+name);
        database.delete(MySqLiteHelper.TABLE_USER, MySqLiteHelper.USER_NAME + "=?", new String[]{name});
        close();
    }

    public void deleteById(String personId) {
        open();
        database.delete(MySqLiteHelper.TABLE_USER, MySqLiteHelper.PERSON_ID + "=?", new String[]{personId});
        close();
    }

    public void clearTable() {
        //执行SQL语句
        open();
        database.delete(MySqLiteHelper.TABLE_USER, MySqLiteHelper.PERSON_ID + ">?", new String[]{"-1"});
        close();
        //        database.execSQL("delete from stu_table where _id  >= 0");
    }

}
