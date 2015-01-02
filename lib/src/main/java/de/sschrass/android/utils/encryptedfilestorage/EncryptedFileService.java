package de.sschrass.android.utils.encryptedfilestorage;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import de.sschrass.android.utils.encryptedfilestorage.Storage.Storage;
import de.sschrass.android.utils.encryptedfilestorage.content.Content;
import de.sschrass.android.utils.encryptedfilestorage.content.ContentDataSource;

public class EncryptedFileService extends Service {
    private static final String TAG = "EncryptedFileService";
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private ContentDataSource contentDataSource;
    private List<Content> contents;
    private Map<String, Integer> progresses = new HashMap<String, Integer>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "creating service");

        HandlerThread handlerThread = new HandlerThread("EncryptedFileServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        serviceLooper = handlerThread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        this.contentDataSource = open(new ContentDataSource(this));
        this.contents = this.contentDataSource.getAllContents();
        purgeAllOutdatedContents(this.contents);

    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) { super(looper); }

        @Override
        public void handleMessage(Message msg) {
            Bundle msgData = msg.getData();
            String url = msgData.getString("de.sschrass.android.utils.encryptedfilestorage.content.url");
            String contentId = msgData.getString("de.sschrass.android.utils.encryptedfilestorage.content.contentId");
            String availabilityEnd = msgData.getString("de.sschrass.android.utils.encryptedfilestorage.content.availabilityEnd");
            Content content = new Content(contentId, availabilityEnd);
            downloadContentEncrypted(url, contentId);
            contentDataSource.createContent(content);
            stopSelf(msg.arg1);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "startCommand received");

        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        message.setData(intent.getExtras());
        serviceHandler.sendMessage(message);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Binding to this service is not implemented.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close(contentDataSource);
    }

    private void downloadContentEncrypted(String url, String contentId) {
        InputStream clearInputStream;
        OutputStream encryptedOutputStream;
        HttpURLConnection urlConnection = null;
        progresses.put(contentId, -1);
        try {
            urlConnection = connect(url);
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
                    this.progresses.put(contentId, (int) ((contentLength / 100) * absProgress));
                }
                cipherOutputStream.write(buffer, offset, length);
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        finally { disconnect(urlConnection); }
    }

    private HttpURLConnection connect(String sUrl) throws Exception {
        URL url = new URL(sUrl);
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

    private void deleteContent(Content content) {
        if (Storage.deleteContentFromStorage(content)) {
            contentDataSource.deleteContent(content);
        }
    }

    public Map<String, Integer> getProgresses() { return progresses; }

    private void purgeAllOutdatedContents(List<Content> contents) {
        for (Content content : contents) {
            try {
                if (ISO8601.toCalendar(content.getAvailabilityEnd()).after(ISO8601.toCalendar(ISO8601.now()))) {
                    deleteContent(content);
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }
    }
}