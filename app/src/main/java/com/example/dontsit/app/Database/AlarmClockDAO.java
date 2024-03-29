package com.example.dontsit.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.dontsit.app.AlarmClockActivity.AlarmClock;
import com.example.dontsit.app.Common.DebugTools;

import java.util.ArrayList;
import java.util.List;

public class AlarmClockDAO {

    private Context context;
    // 表格名稱
    public static final String TABLE_NAME = "AlarmClock";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String REPEAT_COLUMN = "repeat";
    public static final String RESET_COLUMN = "reset";
    public static final String ENABLE_COLUMN = "enable";
    public static final String TIME_COLUMN = "time";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REPEAT_COLUMN + " INTEGER NOT NULL, " +
                    RESET_COLUMN + " INTEGER NOT NULL, " +
                    ENABLE_COLUMN + " INTEGER NOT NULL, " +
                    TIME_COLUMN + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public AlarmClockDAO(Context context) {
        db = NotSitDBHelper.getDatabase(context);
        this.context = context;
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public AlarmClock insert(AlarmClock alarmClock) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(REPEAT_COLUMN, alarmClock.isRepeated() ? 1 : 0);
        cv.put(RESET_COLUMN, alarmClock.isResettable() ? 1 : 0);
        cv.put(ENABLE_COLUMN, alarmClock.isEnabled() ? 1 : 0);
        cv.put(TIME_COLUMN, alarmClock.getTime());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        Long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        alarmClock.setId(id.intValue());

        Intent intent = new Intent(AlarmDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
        intent.putExtra("ID", alarmClock.getId());
        intent.putExtra("Operation",AlarmDatabaseChangedReceiver.ACTION_INSERT);
        context.sendBroadcast(intent, AlarmDatabaseChangedReceiver.PERMISSION_DATABASE_CHANGED);
//        DebugTools.Log("AlarmDatabase insert " + alarmClock);
        // 回傳結果
        return alarmClock;
    }

    // 修改參數指定的物件
    public boolean update(AlarmClock alarmClock) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(REPEAT_COLUMN, alarmClock.isRepeated() ? 1 : 0);
        cv.put(RESET_COLUMN, alarmClock.isResettable() ? 1 : 0);
        cv.put(ENABLE_COLUMN, alarmClock.isEnabled() ? 1 : 0);
        cv.put(TIME_COLUMN, alarmClock.getTime());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + alarmClock.getId();

        Intent intent = new Intent(AlarmDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
        intent.putExtra("ID", alarmClock.getId());
        intent.putExtra("Operation",AlarmDatabaseChangedReceiver.ACTION_UPDATE);
        context.sendBroadcast(intent, AlarmDatabaseChangedReceiver.PERMISSION_DATABASE_CHANGED);
//        DebugTools.Log("AlarmDatabase update " + alarmClock);
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id) {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;

        Intent intent = new Intent(AlarmDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
        Long id2 = id;
        intent.putExtra("ID", id2.intValue());
        intent.putExtra("Operation",AlarmDatabaseChangedReceiver.ACTION_DELETE);
        context.sendBroadcast(intent, AlarmDatabaseChangedReceiver.PERMISSION_DATABASE_CHANGED);

        DebugTools.Log("AlarmDatabase alarm_delete " + id);
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 讀取所有記事資料
    public List<AlarmClock> getAll() {
        List<AlarmClock> result = new ArrayList<AlarmClock>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public AlarmClock get(long id) {
        // 準備回傳結果用的物件
        AlarmClock alarmClock = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            alarmClock = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return alarmClock;
    }

    // 把Cursor目前的資料包裝為物件
    public AlarmClock getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        AlarmClock result = new AlarmClock();

        result.setId(cursor.getInt(0));
        result.setIsRepeated(cursor.getInt(1) == 1);
        result.setIsResettable(cursor.getInt(2) == 1);
        result.setEnabled(cursor.getInt(3) == 1);
        result.setTime(cursor.getInt(4));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        cursor.close();
        return result;
    }

    public void generate() {
        AlarmClock clock = new AlarmClock();
        clock.setTime(2 * 3600 * 1000);
        clock.setIsRepeated(true);
        clock.setIsResettable(true);
        clock.setEnabled(false);
        insert(clock);
        clock.setTime(8 * 3600 * 1000);
        clock.setIsRepeated(true);
        clock.setIsResettable(false);
        insert(clock);
    }
}
