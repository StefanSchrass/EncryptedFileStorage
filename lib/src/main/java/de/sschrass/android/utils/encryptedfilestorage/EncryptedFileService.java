package de.sschrass.android.utils.encryptedfilestorage;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import de.sschrass.android.utils.encryptedfilestorage.content.Content;
import de.sschrass.android.utils.encryptedfilestorage.content.ContentDataSource;

public class EncryptedFileService extends Service {
    private static final String TAG = "EncryptedFileService";
    private ContentDataSource contentDataSource;
    private List<Content> contents;
    private int progress = 0;


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

    private void downloadContentEncrypted(Uri uri, String contentId) throws Exception{
        InputStream clearInputStream = null;
        OutputStream encryptedOutputStream = null;
        HttpURLConnection urlConnection = null;
        this.progress = 0;
        try {
            urlConnection = connect(uri);
            int contentLength = urlConnection.getContentLength();
            clearInputStream = urlConnection.getInputStream();
            encryptedOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + contentId + ".ecd");

            // Key Length should be 128, 192 or 256 bit => i.e. 16 byte
            SecretKeySpec secretKeySpec = new SecretKeySpec("testtesttesttest".getBytes(), "AES"); //TODO algorithm, pw
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptedOutputStream, cipher);
            int length;
            int offset = 0;
            int absProgress = 0;
            byte[] buffer = new byte[8];
            while((length = clearInputStream.read(buffer)) != -1) {
                if (contentLength > -1) {
                    absProgress += length;
                    this.progress = (contentLength / 100) * absProgress;
                } else { this.progress = -1; }
                cipherOutputStream.write(buffer, offset, length);
            }
            cipherOutputStream.flush();
            cipherOutputStream.close();
            clearInputStream.close();
            disconnect(urlConnection);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            disconnect(urlConnection);
        }
    }

    private HttpURLConnection connect(Uri uri) throws Exception {
        URL url = new URL(uri.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();

        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Server returned HTTP " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());
        }
        return urlConnection;
    }

    private void disconnect(HttpURLConnection urlConnection) {
        if (urlConnection != null) { urlConnection.disconnect(); }
    }

    public int getProgress() { return progress; }
}



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