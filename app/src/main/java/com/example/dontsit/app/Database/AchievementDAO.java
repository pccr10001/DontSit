package com.example.dontsit.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.dontsit.app.AchievementActivity.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementDAO {

    // 表格名稱
    public static final String TABLE_NAME = "Achievement";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String NAME_COLUMN = "name";
    public static final String IMAGE_PATH_COLUMN = "image_path";
    public static final String LOCKED_COLUMN = "locked";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String TYPE_COLUMN = "type";
    public static final String SPECDURATION_COLUMN = "spec_duration";
    public static final String SPECVALUE_COLUMN = "spec_value";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_COLUMN + " TEXT NOT NULL, " +
                    IMAGE_PATH_COLUMN + " TEXT NOT NULL, " +
                    LOCKED_COLUMN + " INTEGER NOT NULL, " +
                    TYPE_COLUMN + " INTEGER NOT NULL, " +
                    SPECDURATION_COLUMN + " INTEGER NOT NULL, " +
                    SPECVALUE_COLUMN + " INTEGER NOT NULL, " +
                    DESCRIPTION_COLUMN + " TEXT NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public AchievementDAO(Context context) {
        db = NotSitDBHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public Achievement insert(Achievement achievement) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(NAME_COLUMN, achievement.getName());
        cv.put(IMAGE_PATH_COLUMN, achievement.getImagePath());
        cv.put(LOCKED_COLUMN, achievement.isLocked() ? 1 : 0);
        cv.put(TYPE_COLUMN, achievement.getType());
        cv.put(SPECDURATION_COLUMN, achievement.getSpecDuration());
        cv.put(SPECVALUE_COLUMN, achievement.getSpecValue());
        cv.put(DESCRIPTION_COLUMN, achievement.getDescription());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        Long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        achievement.setId(id.intValue());

        // 回傳結果
        return achievement;
    }

    // 修改參數指定的物件
    public boolean update(Achievement achievement) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(NAME_COLUMN, achievement.getName());
        cv.put(IMAGE_PATH_COLUMN, achievement.getImagePath());
        cv.put(LOCKED_COLUMN, achievement.isLocked() ? 1 : 0);
        cv.put(TYPE_COLUMN, achievement.getType());
        cv.put(SPECDURATION_COLUMN, achievement.getSpecDuration());
        cv.put(SPECVALUE_COLUMN, achievement.getSpecValue());
        cv.put(DESCRIPTION_COLUMN, achievement.getDescription());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + achievement.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id) {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;

        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 讀取所有記事資料
    public List<Achievement> getAll() {
        List<Achievement> result = new ArrayList<Achievement>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public Achievement get(long id) {
        // 準備回傳結果用的物件
        Achievement achievement = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            achievement = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return achievement;
    }

    // 把Cursor目前的資料包裝為物件
    public Achievement getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        Achievement result = new Achievement();

        result.setId(cursor.getInt(0));
        result.setName(cursor.getString(1));
        result.setImagePath(cursor.getString(2));
        result.setLocked(cursor.getString(3).equals("1"));
        result.setType(cursor.getInt(4));
        result.setSpecDuration(cursor.getInt(5));
        result.setSpecValue(cursor.getInt(6));
        result.setDescription(cursor.getString(7));

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

    public void getDefault() {
        List<Achievement> achievements = new ArrayList<Achievement>();
        for (int i = 0; i < 50; i++) {
            Achievement achievement = new Achievement();
            achievement.setType(Achievement.SpecDayType);
            achievement.setLocked(false);
            achievement.setSpecDuration(i + 1);
            achievement.setSpecValue(8);
            achievement.setName("title " + i);
            achievement.setImagePath("drawable/question");
            achievement.setDescription("description " + i);
            achievements.add(achievement);
        }
        for(Achievement achievement : achievements)
            insert(achievement);
    }
}
