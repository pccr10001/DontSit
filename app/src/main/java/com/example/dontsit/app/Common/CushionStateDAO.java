package com.example.dontsit.app.Common;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.dontsit.app.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CushionStateDAO {

    private Context context;
    // 表格名稱
    public static final String TABLE_NAME = "CushionState";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "MAC";

    // 其它表格欄位名稱
    public static final String Duration_COLUMN = "LastTimeDuration";
    public static final String NotifyTime_COLUMN = "LastNotifyTime";
    public static final String ConnectTime_COLUMN = "LastConnectTime";
    public static final String Seated_COLUMN = "Seated";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " TEXT PRIMARY KEY, " +
                    Duration_COLUMN + " INTEGER NOT NULL, " +
                    NotifyTime_COLUMN + " INTEGER NOT NULL, " +
                    ConnectTime_COLUMN + " INTEGER NOT NULL, " +
                    Seated_COLUMN + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public CushionStateDAO(Context context) {
        db = NotSitDBHelper.getDatabase(context);
        this.context = context;
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public Boolean insert(CushionState state) throws ParseException {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_ID, state.getMAC());
        cv.put(Duration_COLUMN, state.getLastTimeDuration());
        cv.put(NotifyTime_COLUMN, state.getLastNotifyTime().getTime());
        cv.put(ConnectTime_COLUMN, state.getLastConnectTime().getTime());
        cv.put(Seated_COLUMN, state.isSeated() ? 1 : 0);

        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        context.sendBroadcast(new Intent(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED));

        // 回傳結果
        return id != -1;
    }

    // 修改參數指定的物件
    public boolean update(CushionState state) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_ID, state.getMAC());
        cv.put(Duration_COLUMN, state.getLastTimeDuration());
        cv.put(NotifyTime_COLUMN, state.getLastNotifyTime().getTime());
        cv.put(ConnectTime_COLUMN, state.getLastConnectTime().getTime());
        cv.put(Seated_COLUMN, state.isSeated() ? 1 : 0);

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "= '" + state.getMAC() + "'";

        Intent intent = new Intent(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
        intent.putExtra(context.getString(R.string.NotifyCushionStateDataKey), state.isSeated());
        context.sendBroadcast(intent);

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(String mac) {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "= '" + mac + "'";

        context.sendBroadcast(new Intent(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED));

        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 讀取所有記事資料
    public List<CushionState> getAll() throws ParseException {
        List<CushionState> result = new ArrayList<CushionState>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public CushionState get(String mac) throws ParseException {
        // 準備回傳結果用的物件
        CushionState state = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "= '" + mac + "'";
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        while (result.moveToNext()) {
            // 讀取包裝一筆資料的物件
            if (getRecord(result).getMAC().equals(mac))
                state = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return state;
    }

    // 把Cursor目前的資料包裝為物件
    public CushionState getRecord(Cursor cursor) throws ParseException {
        // 準備回傳結果用的物件
        CushionState result = new CushionState();

        result.setMAC(cursor.getString(0));
        result.setLastTimeDuration(cursor.getInt(1));
        result.setLastNotifyTime(new Date(cursor.getInt(2)));
        result.setLastConnectTime(new Date(cursor.getInt(3)));
        result.setSeated(cursor.getInt(4) == 1 ? Boolean.TRUE : Boolean.FALSE);

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
        CushionState test = new CushionState("00:15:83:00:3E:95");
        test.setLastConnectTime(Calendar.getInstance().getTime());
        test.setLastNotifyTime(Calendar.getInstance().getTime());
        test.setLastTimeDuration(0);
        test.setSeated(false);
        insert(test);
    }
}
