package de.sschrass.android.utils.encryptedfilestorage;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import de.sschrass.android.utils.encryptedfilestorage.Storage.Storage;
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

        this.contentDataSource = open(new ContentDataSource(this));
        this.contents = this.contentDataSource.getAllContents();
        removeAllOutdatedContents(this.contents);

    }

    private void removeAllOutdatedContents(List<Content> contents) {
        for (Content content : contents) {
            try {
                if (ISO8601.toCalendar(content.getAvailabilityEnd()).after(ISO8601.toCalendar(ISO8601.now()))) {
                    if (Storage.deleteContentFromStorage(content.getContentId())) {
                        contentDataSource.deleteContent(content);
                    }
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Command received");
        Uri uri = intent.getData();
        String contentId = intent.getStringExtra("de.sschrass.android.utils.encryptedfilestorage.content.contentId");
        String availabilityEnd = intent.getStringExtra("de.sschrass.android.utils.encryptedfilestorage.content.availabilityEnd");
        Content content = new Content(contentId, availabilityEnd);

        downloadContentEncrypted(uri, contentId);

        //test
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
        close(contentDataSource);
    }

    private void downloadContentEncrypted(Uri uri, String contentId) {
        InputStream clearInputStream;
        OutputStream encryptedOutputStream;
        HttpURLConnection urlConnection = null;
        this.progress = 0;
        try {
            urlConnection = connect(uri);
            long contentLength = urlConnection.getContentLength();
            clearInputStream = urlConnection.getInputStream();
            encryptedOutputStream = new FileOutputStream(Storage.PATH + "/" + contentId + Storage.EXTENSION);

            // Key Length should be 128, 192 or 256 bit => i.e. 16 byte
            SecretKeySpec secretKeySpec = new SecretKeySpec("testtesttesttest".getBytes(), "AES"); //TODO algorithm, pw
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptedOutputStream, cipher);
            int length;
            int offset = 0;
            long absProgress = 0L;
            byte[] buffer = new byte[8];
            while((length = clearInputStream.read(buffer)) != -1) {
                if (contentLength > -1) {
                    absProgress += length;
                    this.progress = (int) ((contentLength / 100) * absProgress);
                } else { this.progress = -1; }
                cipherOutputStream.write(buffer, offset, length);
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        finally { disconnect(urlConnection); }
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

    private ContentDataSource open(ContentDataSource contentDataSource) {
        try { contentDataSource.open(); }
        catch (SQLException e) { e.printStackTrace(); }
        return contentDataSource;
    }

    private void close(ContentDataSource contentDataSource) {
        if (contentDataSource != null) { contentDataSource.close(); }
    }

    public int getProgress() { return progress; }
}