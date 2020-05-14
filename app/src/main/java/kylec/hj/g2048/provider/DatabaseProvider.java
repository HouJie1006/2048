package kylec.hj.g2048.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.db.GameDatabaseHelper;

public class DatabaseProvider extends ContentProvider {

    public static final int INFO = 0;
    public static final String AUTHORITY = "kylec.hj.g2048.cp";
    private static UriMatcher uriMatcher;
    private GameDatabaseHelper gameDatabaseHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,"info",INFO);
    }

    public DatabaseProvider() {
    }

    @Override
    public boolean onCreate() {
        gameDatabaseHelper = new GameDatabaseHelper(getContext(), Constant.DB_NAME,null,1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = gameDatabaseHelper.getWritableDatabase();
        Cursor cursor = null;
        if (uriMatcher.match(uri) == INFO) {
            cursor = db.query("Info", projection, selection, selectionArgs, null, null, sortOrder);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        if (uriMatcher.match(uri) == INFO) {
            return "vnd.android.cursor.dir/vnd.kylec.hj.g2048.cp.info";
        }
        return null;
    }




    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
