package de.sschrass.android.utils.encryptedfilestorage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contents.db";
    private static int DATABASE_VERSION = 1;
    private static final CursorFactory cursorFactory = null; //null means default

    private static final String TABLE_CONTENTS = "contents";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONTENT_ID = "content_id";
    private static final String COLUMN_CONTENT_AVAILABILITY_END = "availability_end";
    private static final String DATABASE_CREATE = "create table "
            + TABLE_CONTENTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CONTENT_ID + " integer not null, "
            + COLUMN_CONTENT_AVAILABILITY_END + " text not null"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, cursorFactory, DATABASE_VERSION);
    }

    /** is called by the framework, if the database is accessed but not yet created.
     *
     * @param database
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    /**  called, if the database version is increased in your application code. This method allows you to update an existing database schema or to drop the existing database and recreate it via the onCreate() method.
     *
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        this.DATABASE_VERSION = newVersion;
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENTS);
        onCreate(database);
    }
}
