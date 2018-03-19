package Modules;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
/**
 * Created by eeshan on 12/3/18.
 */

public class MyDBHandler extends SQLiteOpenHelper {
    //information of database
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ratingsDB.db";
    public static final String TABLE_NAME = "Ratings";
    public static final String COLUMN_ID = "placeId";
    public static final String COLUMN_NAME = "rating";
    public static final String UNIQUE_ID = "id";
    private static int num = 0;
    //initialize the database
    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + UNIQUE_ID + " INTEGER PRIMARY KEY," + COLUMN_ID +
                " TEXT," + COLUMN_NAME + " REAL)";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
    public float loadHandler(String placeId) {
        float result = 0;
        String query = "Select * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = '" + placeId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount()<=0)
            return -1;
        while (cursor.moveToNext()) {
            cursor.getString(0);
            cursor.getString(1);
            result = cursor.getFloat(2);
        }
        cursor.close();
        db.close();
        return result;
    }
    public void addHandler(String placeId, float rating) {
//        String query = "INSERT OR REPLACE INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ")" + " VALUES ( '" + placeId + "', " + Float.toString(rating) + ")";
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL(query);
        ContentValues v = new ContentValues();
        v.put(COLUMN_ID, placeId);
        v.put(COLUMN_NAME, rating);
        v.put(UNIQUE_ID, num);
        num++;
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, v);
        db.close();
    }
//    public Ratings findHandler(String studentname) {}
//    public boolean deleteHandler(int ID) {}
//    public boolean updateHandler(int ID, String name) {}
}
