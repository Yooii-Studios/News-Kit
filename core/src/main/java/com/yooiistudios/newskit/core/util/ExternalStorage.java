package com.yooiistudios.newskit.core.util;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * ExternalStorage
 *  외장 저장소 파일 입/출력
 */
public class ExternalStorage {
    private static final String SD_CARD_FOLDER_PATH = "/NewsKit";

    public static File getFileFromExternalDirectory(String fileName)
            throws FileNotFoundException, ExternalStorageException {
        File f = new File(createOrGetExternalDirectory(), fileName);

        if (f.exists()){
            return f;
        } else {
            throw new FileNotFoundException();
        }
    }

    public static boolean deleteFileFromExternalDirectory(String fileName)
            throws FileNotFoundException, ExternalStorageException {
        File f = new File(createOrGetExternalDirectory(), fileName);

        if (f.exists()){
            return f.delete();
        } else {
            throw new FileNotFoundException();
        }
    }

    public static File createFileInExternalDirectory(String fileName)
            throws ExternalStorageException {
        try{
            File f = new File(createOrGetExternalDirectory(), fileName);

            if (f.exists()){
                f.delete();
            }
            f.createNewFile();

            return f;
        } catch(IOException e) {
            throw new ExternalStorageException("ERROR occurred during IO.");
        }
    }

    public static File createOrGetExternalDirectory() throws ExternalStorageException {
        checkExternalStorageReadable();
        checkExternalStorageWritable();

        File dir = new File(Environment.getExternalStorageDirectory(), SD_CARD_FOLDER_PATH);
        if (!dir.exists()){
            dir.mkdir();
        }
        return dir;
    }

    private static void checkExternalStorageReadable() throws ExternalStorageNotReadableException {
        if (!isExternalStorageReadable()) {
            throw new ExternalStorageNotReadableException();
        }
    }

    private static void checkExternalStorageWritable() throws ExternalStorageNotWritableException {
        if (!isExternalStorageWritable()) {
            throw new ExternalStorageNotWritableException();
        }
    }
    //

    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static class ExternalStorageException extends Exception {
        public ExternalStorageException() {
        }

        public ExternalStorageException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class ExternalStorageNotReadableException extends ExternalStorageException {
        public ExternalStorageNotReadableException() {
            super("External storage is NOT READABLE.");
        }
    }

    public static class ExternalStorageNotWritableException extends ExternalStorageException {
        public ExternalStorageNotWritableException() {
            super("External storage is NOT WRITABLE.");
        }
    }
}
