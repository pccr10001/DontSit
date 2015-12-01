package com.example.dontsit.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IDIC on 2015/12/1.
 */
public class DurationLogDAO {
    // 表格名稱
    public static final String TABLE_NAME = "Duration";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "StartTime";

    // 其它表格欄位名稱
    public static final String Duration_COLUMN = "Duration";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " TEXT PRIMARY KEY, " +
                    Duration_COLUMN + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public DurationLogDAO(Context context) {
        db = NotSitDBHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public Duration insert(Duration duration) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_ID, duration.getStartTime().toString());
        cv.put(Duration_COLUMN, duration.getTime());

        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件

        // 回傳結果
        return duration;
    }

    @Deprecated
    // 修改參數指定的物件
    public boolean update(Duration duration) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_ID, duration.getStartTime().toString());
        cv.put(Duration_COLUMN, duration.getTime());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "= '" + duration.getStartTime() + "'";

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(String mac) {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "= '" + mac + "'";
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 讀取所有記事資料
    public List<Duration> getAll() throws ParseException {
        List<Duration> result = new ArrayList<Duration>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public Duration get(Date date) throws ParseException {
        // 準備回傳結果用的物件
        Duration duration = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "= '" + date + "'";
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        while (result.moveToNext()) {
            // 讀取包裝一筆資料的物件
            if (getRecord(result).getStartTime().equals(date))
                duration = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return duration;
    }

    // 把Cursor目前的資料包裝為物件
    public Duration getRecord(Cursor cursor) throws ParseException {
        // 準備回傳結果用的物件
        Duration result = new Duration();

        result.setStartTime(DateFormatter.parse(cursor.getString(0)));
        result.setTime(cursor.getInt(1));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext())
            result++;

        cursor.close();
        return result;
    }
}
