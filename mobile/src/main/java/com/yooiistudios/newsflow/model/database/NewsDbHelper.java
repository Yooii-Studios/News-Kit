package com.yooiistudios.newsflow.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yooiistudios.newsflow.core.util.NLLog;

import static com.yooiistudios.newsflow.model.database.NewsDbContract.NewsEntry;
import static com.yooiistudios.newsflow.model.database.NewsDbContract.NewsFeedEntry;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 4.
 *
 * NewsDbHelper
 *  데이터베이스의 생성, 업그레이드 등을 도와주는 클래스
 */
public class NewsDbHelper extends SQLiteOpenHelper {
    private static final String TAG = NewsDbHelper.class.getName();
    public static final String DB_NAME = "NewsArchive.db";
    private static final int DB_VERSION = 3;

    // Macro
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    // SQLs for creating tables
    private static final String SQL_CREATE_NEWSFEED_ENTRY =
            "CREATE TABLE " + NewsFeedEntry.TABLE_NAME + " (" +
                    NewsFeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NewsFeedEntry.COLUMN_NAME_POSITION              + INT_TYPE  + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_TITLE                 + TEXT_TYPE + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_FEED_URL              + TEXT_TYPE + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY     + INT_TYPE  + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_LINK                  + TEXT_TYPE + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_DESCRIPTION           + TEXT_TYPE + COMMA_SEP +
//                    NewsFeedEntry.COLUMN_NAME_IS_VALID              + INT_TYPE  + COMMA_SEP +

                    NewsFeedEntry.COLUMN_NAME_TOPIC_LANGUAGE_CODE   + TEXT_TYPE + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_TOPIC_REGION_CODE     + TEXT_TYPE + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_TOPIC_COUNTRY_CODE    + TEXT_TYPE + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_TOPIC_PROVIDER_ID     + INT_TYPE  + COMMA_SEP +
                    NewsFeedEntry.COLUMN_NAME_TOPIC_ID              + INT_TYPE  +
            " )";

    private static final String SQL_CREATE_NEWS_ENTRY =
            "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +
                    NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NewsEntry.COLUMN_NAME_FEED_POSITION     + INT_TYPE  + COMMA_SEP +
                    NewsEntry.COLUMN_NAME_INDEX             + INT_TYPE  + COMMA_SEP +
                    NewsEntry.COLUMN_NAME_TITLE             + TEXT_TYPE + COMMA_SEP +
                    NewsEntry.COLUMN_NAME_LINK              + TEXT_TYPE + COMMA_SEP +
                    NewsEntry.COLUMN_NAME_DESCRIPTION       + TEXT_TYPE + COMMA_SEP +
                    NewsEntry.COLUMN_NAME_IMAGE_URL         + TEXT_TYPE + COMMA_SEP +
                    NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED + INT_TYPE  + COMMA_SEP +
                    "FOREIGN KEY(" + NewsEntry.COLUMN_NAME_FEED_POSITION + ")" +
                    " REFERENCES " + NewsFeedEntry.TABLE_NAME + "(" + NewsFeedEntry.COLUMN_NAME_POSITION + ")" +
                    " )";

    public static final String SQL_DROP_NEWSFEED_ENTRY = "DROP TABLE " + NewsFeedEntry.TABLE_NAME;
    public static final String SQL_DROP_NEWS_ENTRY = "DROP TABLE " + NewsEntry.TABLE_NAME;

    public NewsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
    }

    private void createAllTables(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NEWSFEED_ENTRY);
        db.execSQL(SQL_CREATE_NEWS_ENTRY);
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.execSQL(SQL_DROP_NEWSFEED_ENTRY);
        db.execSQL(SQL_DROP_NEWS_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 구조가 바뀐 경우(데이터베이스에 컬럼이 추가되거나 새 테이블이 추가된 경우 등)
        // DB_VERSION 을 증가시키고 버전 체크를 해 필요한 처리를 한다.
        NLLog.i(TAG, "oldVersion : " + oldVersion + "\nnewVersion : " + newVersion);
        if (oldVersion < 3) {
            dropAllTables(db);
            createAllTables(db);
        }
        /*
        if (oldVersion < 2) {
            String oldTableName = NewsFeedEntry.TABLE_NAME;
            String tempTableName = "TEMP";
            String columnsToCopyInStr =
                    NewsFeedEntry.COLUMN_NAME_POSITION                + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_TITLE                 + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_FEED_URL              + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY     + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_LINK                  + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_DESCRIPTION           + COMMA_SEP

                    + NewsFeedEntry.COLUMN_NAME_TOPIC_REGION_CODE     + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_TOPIC_LANGUAGE_CODE   + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_TOPIC_PROVIDER_ID     + COMMA_SEP
                    + NewsFeedEntry.COLUMN_NAME_TOPIC_ID;

            String createTableSql = "CREATE TEMP TABLE " + tempTableName + " AS"
                    + " SELECT "
                    + columnsToCopyInStr
                    + " FROM " + oldTableName;
            String dropTableSql = "DROP TABLE " + oldTableName;

            String restoreBackupDataSql = "INSERT INTO " + oldTableName
                    + " (" + columnsToCopyInStr + ")"
                    + " SELECT "
                    + columnsToCopyInStr
                    +" FROM " + tempTableName;

            db.beginTransaction();
            try {
                db.execSQL(createTableSql);
                db.execSQL(dropTableSql);
                db.execSQL(SQL_CREATE_NEWSFEED_ENTRY);
                db.execSQL(restoreBackupDataSql);

                db.setTransactionSuccessful();

                NLLog.i(TAG, "TransactionSuccessful");
            } catch (Exception e) {
                e.printStackTrace();
                NLLog.i(TAG, e.getMessage());
            } finally {
                db.endTransaction();
            }
        }
        */
    }
}
