package com.example.dontsit.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.dontsit.app.Common.DateFormatter;
import com.example.dontsit.app.Common.DebugTools;

import java.text.ParseException;
import java.util.*;

public class DayDurationLogDAO {
    // 表格名稱
    public static final String TABLE_NAME = "DayDuration";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "Id";

    // 其它表格欄位名稱
    public static final String Date_COLUMN = "Date";
    public static final String SitTime_COLUMN = "SitTime";
    public static final String ChangeTime_COLUMN = "ChangeTime";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Date_COLUMN + " TEXT NOT NULL, " +
                    SitTime_COLUMN + " INTEGER NOT NULL, " +
                    ChangeTime_COLUMN + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public DayDurationLogDAO(Context context) {
        db = NotSitDBHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public DayDuration insert(DayDuration duration) throws ParseException {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

//        DebugTools.Log("insert " + duration);
        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(Date_COLUMN, DateFormatter.middle_format(duration.getDate()));
        cv.put(SitTime_COLUMN, duration.getSitTime());
        cv.put(ChangeTime_COLUMN, duration.getChangeTime());

        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        Long id = db.insert(TABLE_NAME, null, cv);
        duration.setId(id.intValue());

        // 回傳結果
        return duration;
    }

    // 刪除參數指定編號的資料
    public boolean delete(Date date) throws ParseException {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = Date_COLUMN + "= '" + DateFormatter.middle_format(date) + "'";
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public void removeAll() {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will alarm_delete all rows.
        db.execSQL("delete from " + TABLE_NAME);
    }

    // 讀取所有記事資料
    public List<DayDuration> getAll() throws ParseException {
        List<DayDuration> result = new ArrayList<DayDuration>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public List<DayDuration> getByRange(int day) throws ParseException {
        // 準備回傳結果用的物件
        List<DayDuration> result = new ArrayList<DayDuration>();
        // 使用編號為查詢條件
        // 執行查詢
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, KEY_ID + " DESC", String.valueOf(day));

        // 如果有查詢結果
        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

//        DebugTools.Log(result);

        cursor.close();
        return result;
    }

    public DayDuration get(Date date) throws ParseException {
        // 準備回傳結果用的物件
        DayDuration duration = null;
        // 使用編號為查詢條件
        String where = Date_COLUMN + "= '" + DateFormatter.middle_format(date) + "'";
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        while (result.moveToNext()) {
            // 讀取包裝一筆資料的物件
            duration = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return duration;
    }

    // 把Cursor目前的資料包裝為物件
    public DayDuration getRecord(Cursor cursor) throws ParseException {
        // 準備回傳結果用的物件
        DayDuration result = new DayDuration();

        result.setId(cursor.getInt(0));
        result.setDate(DateFormatter.middle_parse(cursor.getString(1)));
        result.setSitTime(cursor.getInt(2));
        result.setChangeTime(cursor.getInt(3));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext())
            result = cursor.getInt(0);

        cursor.close();
        return result;
    }

    public void generate() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        int count = 365, gap = 8;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        List<DayDuration> durations = new ArrayList<DayDuration>();
        for (int i = 0; i < count; i++) {
            DayDuration duration = new DayDuration();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            duration.setDate(calendar.getTime());
            duration.setSitTime((3600 * gap / 2) + new Random().nextInt(3600 * (gap / 2 + 1)));
            duration.setChangeTime(new Random().nextInt(20));
            durations.add(duration);
//            DebugTools.Log(duration);
        }
        Collections.reverse(durations);
        for (DayDuration duration : durations)
            insert(duration);
    }
}
