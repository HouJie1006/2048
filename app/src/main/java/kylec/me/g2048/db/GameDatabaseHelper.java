package kylec.me.g2048.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * 创建数据库
 */
public class GameDatabaseHelper extends SQLiteOpenHelper {

private static final String CREATE_G4 = "create table if not exists G4 ("
        + "id integer primary key autoincrement, "
        + "x integer, "
        + "y integer, "
        + "num integer)";

    private static final String INFO = "create table if not exists info ("
            + " id integer primary key,"
            + " user_name varchar,"
            + " score integer ,"
            + "time varchar)";

    public GameDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_G4);
        db.execSQL(INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists G4");
        db.execSQL("drop table if exists info");
        onCreate(db);
    }
}
