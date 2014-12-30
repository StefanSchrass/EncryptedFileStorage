package de.sschrass.android.utils.encryptedfilestorage.content;

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
            databaseHelper.COLUMN_ID,
            databaseHelper.COLUMN_CONTENT_ID,
            databaseHelper.COLUMN_CONTENT_AVAILABILITY_END
    };

    public ContentDataSource(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
    }

    public void deleteComment(Content content) {
        long id = content.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public List<Content> getAllComments() {
        List<Content> comments = new ArrayList<Content>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_CONTENTS, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Content content = cursorToComment(cursor);
            comments.add(content);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    private Content cursorToComment(Cursor cursor) {
        return new Content(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
    }
}
