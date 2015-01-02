package de.sschrass.android.utils.encryptedfilestorage.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.sschrass.android.utils.encryptedfilestorage.database.DatabaseHelper;

public class ContentDataSource {
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;
    private String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_CONTENT_ID,
            DatabaseHelper.COLUMN_CONTENT_AVAILABILITY_END
    };

    public ContentDataSource(Context context) { databaseHelper = new DatabaseHelper(context); }
    public void open() throws SQLException { database = databaseHelper.getWritableDatabase(); }
    public void close() { databaseHelper.close(); }

    public Content createContent(Content content) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CONTENT_ID, content.getContentId());
        values.put(DatabaseHelper.COLUMN_CONTENT_AVAILABILITY_END, content.getAvailabilityEnd());
        final String nullColumnHack = null;
        long insertId = database.insert(DatabaseHelper.TABLE_CONTENTS, nullColumnHack, values);
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_CONTENTS, allColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, selectionArgs, groupBy, having, orderBy);
        cursor.moveToFirst();
        Content newContent = cursorToContent(cursor);
        cursor.close();
        return newContent;
    }

    public List<Content> getAllContents() {
        List<Content> contents = new ArrayList<Content>();
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_CONTENTS, allColumns, selection, selectionArgs, groupBy, having, orderBy);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Content content = cursorToContent(cursor);
            contents.add(content);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return contents;
    }

    // TODO update

    public void deleteContent(Content content) {
        long id = content.getId();

        if (id > -1L) {
            String[] whereArgs = null;
            database.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.COLUMN_ID + " = " + id, whereArgs);
        }
    }

    private Content cursorToContent(Cursor cursor) {
        return new Content(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
    }
}
