package de.sschrass.android.utils.encryptedfilestorage.Storage;

import android.os.Environment;

import java.io.File;

public class Storage {
    public static final String PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String EXTENSION = ".ecd";

    public static boolean deleteContentFromStorage(String contentId) {
        File file = new File(PATH + "/" + contentId + EXTENSION);
        return file.delete();
    }
}
