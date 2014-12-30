package de.sschrass.android.utils.encryptedfilestorage.content;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import de.sschrass.android.utils.encryptedfilestorage.database.DatabaseHelper;

public class ContentDownloadService extends Service {
    private static final String TAG = "ContentDownloadService";
    public ContentDownloadService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Command received");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
