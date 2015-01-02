package de.sschrass.android.utils.encryptedfilestorage;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import de.sschrass.android.utils.encryptedfilestorage.content.Content;
import de.sschrass.android.utils.encryptedfilestorage.content.ContentDataSource;

public class EncryptedFileService extends Service {
    private static final String TAG = "EncryptedFileService";
    private ContentDataSource contentDataSource;
    private List<Content> contents;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "creating service");

        contentDataSource = new ContentDataSource(this);
        try { contentDataSource.open(); }
        catch (SQLException e) { e.printStackTrace(); }
        contents = contentDataSource.getAllContents();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Command received");
        Uri uri = intent.getData();
        String contentId = intent.getStringExtra("de.sschrass.android.utils.encryptedfilestorage.content.contentId");
        String availabilityEnd = intent.getStringExtra("de.sschrass.android.utils.encryptedfilestorage.content.availabilityEnd");
        Content content = new Content(contentId, availabilityEnd);

        contentDataSource.createContent(content);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (contentDataSource != null) { contentDataSource.close(); }
    }

    private void downloadContentAndEncrypt(Uri uri, String contentId) throws Exception{
        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned HTTP " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());
            }

            int fileLength = urlConnection.getContentLength();
            inputStream = urlConnection.getInputStream();
            outputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + contentId + ".enc");
        }
        catch (IOException ioe) { ioe.printStackTrace(); }
    }
}



//    @Override
//    protected String doInBackground(String... sUrl) {
//        InputStream input = null;
//        OutputStream output = null;
//        HttpURLConnection connection = null;
//        try {
//            URL url = new URL(sUrl[0]);
//            connection = (HttpURLConnection) url.openConnection();
//            connection.connect();
//
//            // expect HTTP 200 OK, so we don't mistakenly save error report
//            // instead of the file
//            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                return "Server returned HTTP " + connection.getResponseCode()
//                        + " " + connection.getResponseMessage();
//            }
//
//            // this will be useful to display download percentage
//            // might be -1: server did not report the length
//            int fileLength = connection.getContentLength();
//
//            // download the file
//            input = connection.getInputStream();
//            output = new FileOutputStream("/sdcard/file_name.extension");
//
//            byte data[] = new byte[4096];
//            long total = 0;
//            int count;
//            while ((count = input.read(data)) != -1) {
//                // allow canceling with back button
//                if (isCancelled()) {
//                    input.close();
//                    return null;
//                }
//                total += count;
//                // publishing the progress....
//                if (fileLength > 0) // only if total length is known
//                    publishProgress((int) (total * 100 / fileLength));
//                output.write(data, 0, count);
//            }
//        } catch (Exception e) {
//            return e.toString();
//        } finally {
//            try {
//                if (output != null)
//                    output.close();
//                if (input != null)
//                    input.close();
//            } catch (IOException ignored) {
//            }
//
//            if (connection != null)
//                connection.disconnect();
//        }
//        return null;
//    }