package com.example.dontsit.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.dontsit.app.Common.DateFormatter;
import com.example.dontsit.app.Common.DebugTools;

import java.text.ParseException;
import java.util.*;

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
                    KEY_ID + " DATETIME PRIMARY KEY, " +
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
    public Duration insert(Duration duration) throws ParseException {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

//        DebugTools.Log("insert " + duration);
        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(KEY_ID, DateFormatter.format(duration.getStartTime()));
        cv.put(Duration_COLUMN, duration.getTime() / 1000);

        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        db.insert(TABLE_NAME, null, cv);

        // 回傳結果
        return duration;
    }

    // 刪除參數指定編號的資料
    public boolean delete(Date date) throws ParseException {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "= '" + DateFormatter.format(date) + "'";
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean deleteBefore(Date date) throws ParseException {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + " < '" + DateFormatter.format(date) + "'";
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public void removeAll() {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        db.execSQL("delete from " + TABLE_NAME);
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

    public List<Duration> getBetween(Date date1, Date date2) throws ParseException {
        // 準備回傳結果用的物件
        List<Duration> result = new ArrayList<Duration>();
        // 使用編號為查詢條件
        String where = KEY_ID + " > '" + DateFormatter.format(date1) +
                "' AND " + KEY_ID + " <= '" + DateFormatter.format(date2) + "'";

        // 執行查詢
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

//        DebugTools.Log(result);

        cursor.close();
        return result;
    }

    public List<Duration> getBefore(Date date) throws ParseException {
        return getBefore(date, 0);
    }

    public List<Duration> getBefore(Date date, int num) throws ParseException {
        // 準備回傳結果用的物件
        List<Duration> result = new ArrayList<Duration>();
        // 使用編號為查詢條件

        String where = KEY_ID + " < '" + DateFormatter.format(date) + "'";

        Cursor cursor;
        // 執行查詢
        if (num != 0)
            cursor = db.query(
                    TABLE_NAME, null, where, null, null, null, KEY_ID + " DESC", String.valueOf(num));
        else {
            cursor = db.query(
                    TABLE_NAME, null, where, null, null, null, KEY_ID + " DESC", null);
        }

        // 如果有查詢結果
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
        String where = KEY_ID + "= " + date + "";
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

    public int getHourTimeAt(Date date) throws ParseException {
//        DebugTools.Log("target = " + date);
        int count = 0;
        Calendar calendar1 = Calendar.getInstance(), calendar2 = Calendar.getInstance();
        calendar1.setTimeZone(TimeZone.getDefault());
        calendar2.setTimeZone(TimeZone.getDefault());
        calendar1.setTime(date);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar2.setTime(calendar1.getTime());
        calendar1.add(Calendar.HOUR, -1);

        List<Duration> durations = getBetween(calendar1.getTime(), calendar2.getTime());
        List<Duration> temp = getBefore(calendar1.getTime(), 1);

        if (durations.size() > 0) {

            for (int i = 0; i < durations.size() - 1; i++)
                count += durations.get(i).getTime() / 1000;

            Duration last = durations.get(durations.size() - 1);
            calendar1.setTime(last.getStartTime());

            int left = ((59 - calendar1.get(Calendar.MINUTE)) * 60
                    + (60 - calendar1.get(Calendar.SECOND)));
            count += last.getTime() / 1000 < left ? last.getTime() / 1000 : left;
        }

        if (temp.size() > 0) {
            Duration last = temp.get(0);

            calendar1.setTime(date);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);

            if (last.getTime() / 1000 > calendar1.getTime().getTime() / 1000
                    - last.getStartTime().getTime() / 1000) {
                calendar2.setTime(last.getStartTime());
                long left = (last.getTime().longValue() / 1000 -
                        (calendar1.getTime().getTime() / 1000 - last.getStartTime().getTime() / 1000));
                count += left > 3600 ? 3600 : left;
            }
        }
        return count;
    }

    public int getHourTimesAt(Date date) throws ParseException {
        Calendar calendar1 = Calendar.getInstance(),
                calendar2 = Calendar.getInstance();
        calendar1.setTimeZone(TimeZone.getDefault());
        calendar2.setTimeZone(TimeZone.getDefault());
        calendar1.setTime(date);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar2.setTime(date);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        return getBetween(calendar1.getTime(), calendar2.getTime()).size();
    }

    public int getDayTimeAt(Date date) throws ParseException {
//        DebugTools.Log("target = " + date);
        int count = 0;
        Calendar calendar1 = Calendar.getInstance(), calendar2 = Calendar.getInstance();
        calendar1.setTimeZone(TimeZone.getDefault());
        calendar2.setTimeZone(TimeZone.getDefault());
        calendar1.setTime(date);
        calendar1.set(Calendar.HOUR_OF_DAY, 23);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar2.setTime(calendar1.getTime());
        calendar1.add(Calendar.DAY_OF_MONTH, -1);

        List<Duration> durations = getBetween(calendar1.getTime(), calendar2.getTime());
        List<Duration> temp = getBefore(calendar1.getTime(), 1);
//        DebugTools.Log(durations);
//        DebugTools.Log(temp);

        if (durations.size() > 0) {

            for (int i = 0; i < durations.size() - 1; i++)
                count += durations.get(i).getTime() / 1000;

            Duration last = durations.get(durations.size() - 1);
            calendar1.setTime(last.getStartTime());

            int left = ((23 - calendar1.get(Calendar.HOUR_OF_DAY)) * 3600
                    + ((59 - calendar1.get(Calendar.MINUTE)) * 60
                    + (60 - calendar1.get(Calendar.SECOND))));
//            DebugTools.Log("last.getTime() / 1000 " + last.getTime() / 1000);
//            DebugTools.Log("left " + left);
            count += last.getTime() / 1000 < left ? last.getTime() / 1000 : left;
        }


        if (temp.size() > 0) {
            Duration last = temp.get(0);

            calendar1.setTime(date);
            calendar1.set(Calendar.HOUR_OF_DAY, 0);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);

            if (last.getTime() / 1000 > calendar1.getTime().getTime() / 1000
                    - last.getStartTime().getTime() / 1000) {
                calendar2.setTime(last.getStartTime());
                long left = (last.getTime().longValue() / 1000 -
                        (calendar1.getTime().getTime() / 1000 - last.getStartTime().getTime() / 1000));
                count += left > 86400 ? 86400 : left;
            }
        }
//        DebugTools.Log(count);

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeZone(TimeZone.getDefault());
//        calendar.setTime(date);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        int count = 0;
//        for (int i = 0; i < 24; i++) {
////            DebugTools.Log(calendar.get(Calendar.HOUR_OF_DAY));
//            count += getHourTimeAt(calendar.getTime());
//            calendar.add(Calendar.HOUR_OF_DAY, -1);
//        }
        return count;
    }

    public int getDayTimesAt(Date date) throws ParseException {
        Calendar calendar1 = Calendar.getInstance(),
                calendar2 = Calendar.getInstance();
        calendar1.setTimeZone(TimeZone.getDefault());
        calendar2.setTimeZone(TimeZone.getDefault());
        calendar1.setTime(date);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar2.setTime(date);
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        DebugTools.Log(calendar1.getTime());
        DebugTools.Log(calendar2.getTime());
        DebugTools.Log(getBetween(calendar1.getTime(), calendar2.getTime()));
        return getBetween(calendar1.getTime(), calendar2.getTime()).size();
    }

    // 把Cursor目前的資料包裝為物件
    public Duration getRecord(Cursor cursor) throws ParseException {
        // 準備回傳結果用的物件
        Duration result = new Duration();

        result.setStartTime(DateFormatter.parse(cursor.getString(0)));
        result.setTime(cursor.getInt(1) * 1000);

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
        calendar.setTimeZone(TimeZone.getDefault());
        int gap = 3, count = calendar.get(Calendar.HOUR_OF_DAY) / gap + 24 / gap * 6;
        List<Duration> durations = new ArrayList<Duration>();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        for (int i = 0; i < count; i++) {
            Duration duration = new Duration();
            calendar.add(Calendar.HOUR_OF_DAY, -gap);
            duration.setStartTime(calendar.getTime());
            if (new Random().nextInt(24 / gap + 1) < 48 / gap / gap) {
                duration.setTime(((3600 * gap / 2) + new Random().nextInt(3600 * (gap / 2 + 1))) * 1000);
                durations.add(duration);
            }
            DebugTools.Log(duration);
        }
        for (Duration duration : durations)
            insert(duration);
    }
}
