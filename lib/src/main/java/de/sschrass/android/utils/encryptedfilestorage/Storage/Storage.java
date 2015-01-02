package de.sschrass.android.utils.encryptedfilestorage.Storage;

import android.os.Environment;

import java.io.File;

import de.sschrass.android.utils.encryptedfilestorage.content.Content;

public class Storage {
    public static final String PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String EXTENSION = ".ecd";

    public static boolean deleteContentFromStorage(Content content) {
        File file = new File(PATH + "/" + content.getId() + EXTENSION);
        return file.delete();
    }
}
