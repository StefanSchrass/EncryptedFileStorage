package de.sschrass.android.utils.encryptedfilesafe;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ContentDownloadService extends Service {
    private static final String TAG = "ContentDownloadService";
    public ContentDownloadService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        DatabaseHelper databaseHelper = new DatabaseHelper();

    }

    public static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
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
