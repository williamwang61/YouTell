package luckynine.youtell.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import luckynine.youtell.data.DataContract.PostEntry;
import luckynine.youtell.data.DataContract.LocationEntry;
/**
 * Created by Weiliang on 6/22/2015.
 */
public class PostDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "post.db";

    public PostDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COUNTRY + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry.COLUMN_ID + " TEXT PRIMARY KEY, " +
                PostEntry.COLUMN_AUTHOR_ID + " TEXT NOT NULL, " +
                PostEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                PostEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                PostEntry.COLUMN_CREATED_AT + " DATETIME NOT NULL, " +
                PostEntry.COLUMN_LOCATION_ID + " INTEGER, " +
                " FOREIGN KEY(" + PostEntry.COLUMN_LOCATION_ID + ") REFERENCES " +
                LocationEntry.TABLE_NAME + "(" + LocationEntry.COLUMN_ID + ")" +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_POST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
