package com.yooiistudios.newsflow.core.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 */
public class ExternalStorage {
    private static final String SD_CARD_FOLDER_PATH = "/Path Finder";

    /**
     *
     * @param fileName
     * @return null if file not exists.
     */
    public static File getFileFromExternalDirectory(String fileName) {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File f = new File(createExternalDirectory(), fileName);

            if (f.exists()){
                return f;
            }
        }

        return null;
    }
    public static boolean deleteFileFromExternalDirectory(String fileName) {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File f = new File(createExternalDirectory(), fileName);

            if (f.exists()){
                return f.delete();
            }
        }

        return false;
    }

    /**
     * created directory as file. Make new directory if there's no directory exists.
     * @return null if problem occurred.
     */
    public static File createExternalDirectory() {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File dir = new File(Environment.getExternalStorageDirectory(), SD_CARD_FOLDER_PATH);
            if (!dir.exists()){
                dir.mkdir();
            }
            return dir;
        }
        return null;
    }

    /**
     * create file in external directory. file deleted if exists.
     * @param fileName
     * @return
     */
    public static File createFileInExternalDirectory(Context context, String fileName) {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            try{
                File f = new File(createExternalDirectory(), fileName);

                if (f.exists()){
                    f.delete();
                }

                boolean created = f.createNewFile();
                if (created){
                    return f;
                }
            } catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
