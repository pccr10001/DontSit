package com.example.dontsit.app.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotSitDBHelper extends SQLiteOpenHelper {

    // 資料庫名稱
    public static final String DATABASE_NAME = "NotSit.db";
    // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
    public static final int VERSION = 1;
    // 資料庫物件，固定的欄位變數
    private static SQLiteDatabase database;

    // 建構子，在一般的應用都不需要修改
    public NotSitDBHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new NotSitDBHelper(context, DATABASE_NAME,
                    null, VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建立應用程式需要的表格
        db.execSQL(DurationLogDAO.CREATE_TABLE);
        db.execSQL(DayDurationLogDAO.CREATE_TABLE);
        db.execSQL(AlarmClockDAO.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 刪除原有的表格
        db.execSQL("DROP TABLE IF EXISTS " + DurationLogDAO.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DayDurationLogDAO.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlarmClockDAO.TABLE_NAME);
        // 呼叫onCreate建立新版的表格
        onCreate(db);
    }
}
