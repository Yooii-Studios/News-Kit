package com.yooiistudios.newskit.core.news.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.yooiistudios.newskit.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsFeedFetchState;
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.newscontent.NewsContent;
import com.yooiistudios.newskit.core.news.newscontent.NewsContentFetchState;
import com.yooiistudios.newskit.core.util.ExternalStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import static com.yooiistudios.newskit.core.cache.volley.CacheImageLoader.PaletteColor;
import static com.yooiistudios.newskit.core.news.database.NewsDbContract.NewsContentEntry;
import static com.yooiistudios.newskit.core.news.database.NewsDbContract.NewsEntry;
import static com.yooiistudios.newskit.core.news.database.NewsDbContract.NewsFeedEntry;
import static com.yooiistudios.newskit.core.news.database.NewsDbContract.PaletteColorEntry;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 4.
 *
 * NewsDb
 *  쿼리, 삽입, 삭제, 업데이트 기능 래핑
 */
public class NewsDb {
    private static final boolean DEBUG_BLOCK_SHUFFLE = false;

    private NewsDbHelper mHelper;
    private SQLiteDatabase mDatabase;

    /**
     * Singleton
     */
    private volatile static NewsDb instance;
    public static NewsDb getInstance(Context context) {
        if (instance == null) {
            synchronized (NewsDb.class) {
                if (instance == null) {
                    instance = new NewsDb(context);
                }
            }
        }
        return instance;
    }

    private NewsDb(Context context) {
        mHelper = new NewsDbHelper(context);
        open();
    }

    private NewsDb open() throws SQLException {
        if (!isOpen()) {
            mDatabase = mHelper.getWritableDatabase();
        }
        return this;
    }

    /*
    // close 를 굳이 하지 않아도 open 만 사용해도 될 것으로 판단해 일단 사용하지 않음
    private void close() {
        if (isOpen()) {
            mDatabase.close();
        }
        if (mHelper != null) {
            mHelper.close();
        }
    }
    */

    private boolean isOpen() {
        return mDatabase != null && mDatabase.isOpen();
    }

    public NewsFeed loadTopNewsFeed(Context context) {
        return loadTopNewsFeed(context, true);
    }

    public NewsFeed loadTopNewsFeed(Context context, boolean shuffle) {
        NewsFeed newsFeed = queryNewsFeed(NewsFeed.INDEX_TOP, shuffle);
        if (newsFeed == null) {
            return DefaultNewsFeedProvider.getDefaultTopNewsFeed(context);
        } else {
            return newsFeed;
        }
    }

    public ArrayList<NewsFeed> loadBottomNewsFeedList(Context context, int panelCount) {
        return loadBottomNewsFeedList(context, panelCount, true);
    }

    public ArrayList<NewsFeed> loadBottomNewsFeedList(Context context, int panelCount,
                                                              boolean shuffle) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        mDatabase.beginTransaction();
        try {
            newsFeeds = loadBottomNewsFeedListInternal(context, panelCount, shuffle);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }

        return newsFeeds;
    }

    private ArrayList<NewsFeed> loadBottomNewsFeedListInternal(Context context, int panelCount,
                                                              boolean shuffle) {
        String[] newsFeedWhereArgs = {
                String.valueOf(NewsFeed.INDEX_BOTTOM_START),
                String.valueOf(panelCount)
        };

        Cursor newsFeedCursor = mDatabase.query(
                NewsFeedEntry.TABLE_NAME,
                null,
                NewsFeedEntry.COLUMN_NAME_POSITION + " >= ?" +
                        " and " + NewsFeedEntry.COLUMN_NAME_POSITION + " < ?",
                newsFeedWhereArgs,
                null, null, NewsFeedEntry.COLUMN_NAME_POSITION);
        newsFeedCursor.moveToFirst();

        ArrayList<NewsFeed> newsFeedList = new ArrayList<>();
        if (newsFeedCursor.getCount() <= 0) {
            // no saved top news feed
            newsFeedCursor.close();

            ArrayList<NewsFeed> defaultNewsFeedList =
                    DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(context);
            saveBottomNewsFeedList(defaultNewsFeedList);
            return defaultNewsFeedList;
        }
        while (!newsFeedCursor.isAfterLast()) {
            NewsFeed newsFeed = convertCursorToNewsFeed(newsFeedCursor);

            int newsFeedPosition = newsFeedCursor.getInt(
                    newsFeedCursor.getColumnIndex(NewsFeedEntry.COLUMN_NAME_POSITION));
            newsFeed.setNewsList(queryNewsListByNewsFeedPosition(newsFeedPosition, shuffle));

            newsFeedList.add(newsFeed);

            newsFeedCursor.moveToNext();
        }

        newsFeedCursor.close();

        int newsFeedCount = newsFeedList.size();
        if (newsFeedCount > panelCount) {
            newsFeedList = new ArrayList<>(newsFeedList.subList(0, panelCount));
        } else if (newsFeedCount < panelCount) {
            ArrayList<NewsFeed> defaultNewsFeedList =
                    DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(context);
            for (int idx = newsFeedCount; idx < panelCount; idx++) {
                newsFeedList.add(defaultNewsFeedList.get(idx));
            }
        }
        return newsFeedList;
    }

    public NewsFeed loadBottomNewsFeedAt(Context context, int position, boolean shuffle) {
        NewsFeed newsFeed = queryNewsFeed(position, shuffle);

        if (newsFeed == null) {
            // cache 된 내용 없는 경우 새로 만들어서 리턴
            ArrayList<NewsFeed> newsFeedList =
                    DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(context);
            if (position < newsFeedList.size()) {
                newsFeed = newsFeedList.get(position);
            } else {
                newsFeed = newsFeedList.get(0);
            }
            saveBottomNewsFeedAt(newsFeed, position);
        }
        return newsFeed;
    }

    public void saveTopNewsFeed(NewsFeed newsFeed) {
        insertNewsFeed(newsFeed, NewsFeed.INDEX_TOP);
    }

    public void saveBottomNewsFeedList(ArrayList<NewsFeed> bottomNewsFeedList) {
        mDatabase.beginTransaction();
        try {
            saveBottomNewsFeedListInternal(bottomNewsFeedList);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    private void saveBottomNewsFeedListInternal(ArrayList<NewsFeed> bottomNewsFeedList) {
        for (int idx = 0; idx < bottomNewsFeedList.size(); idx++) {
            insertNewsFeed(bottomNewsFeedList.get(idx), idx);
        }
    }

    public void saveBottomNewsFeedAt(NewsFeed bottomNewsFeed, int position) {
        insertNewsFeed(bottomNewsFeed, position);
    }

    public void saveNewsContentWithGuid(News news) {
        mDatabase.beginTransaction();
        try {
            saveNewsContentWithGuidInternal(news);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    private void saveNewsContentWithGuidInternal(News news) {
        if (!news.hasNewsContent()) {
            return;
        }
        NewsContent newsContent = news.getNewsContent();
        String guid = news.getGuid();

        ContentValues newsFeedValues = new ContentValues();
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_GUID, guid);
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_URL, newsContent.getUrl());
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_TITLE, newsContent.getTitle());
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_TEXT, newsContent.getText());
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_VIDEO_URL, newsContent.getVideoUrl());
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_FETCH_STATE, newsContent.getFetchState().getIndex());

        mDatabase.delete(NewsContentEntry.TABLE_NAME, NewsContentEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ guid });
        mDatabase.insert(NewsContentEntry.TABLE_NAME, null, newsFeedValues);
    }

    public void saveTopNewsImageUrlWithGuid(String imageUrl, String guid) {
        insertNewsImage(imageUrl, NewsFeed.INDEX_TOP, guid);
    }

    public void saveBottomNewsImageUrlWithGuid(String imageUrl,
                                               int newsFeedPosition, String guid) {
        insertNewsImage(imageUrl, newsFeedPosition, guid);
    }

    public void savePaletteColor(int newsFeedPosition, String guid, PaletteColor paletteColor) {
        mDatabase.beginTransaction();
        try {
            savePaletteColorInternal(newsFeedPosition, guid, paletteColor);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void savePaletteColorInternal(int newsFeedPosition, String guid,
                                         PaletteColor paletteColor) {
        ContentValues colorValues = new ContentValues();
        colorValues.put(PaletteColorEntry.COLUMN_NAME_FEED_POSITION, newsFeedPosition);
        colorValues.put(PaletteColorEntry.COLUMN_NAME_GUID, guid);
        colorValues.put(PaletteColorEntry.COLUMN_NAME_PALETTE_COLOR, paletteColor.getPaletteColor());
        colorValues.put(PaletteColorEntry.COLUMN_NAME_TYPE, paletteColor.getType().ordinal());

        mDatabase.delete(PaletteColorEntry.TABLE_NAME,
                PaletteColorEntry.COLUMN_NAME_FEED_POSITION + "=? and " +
                        PaletteColorEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ String.valueOf(newsFeedPosition), guid });
        mDatabase.insert(PaletteColorEntry.TABLE_NAME, null, colorValues);
    }

//    public void savePaletteColorInternal(String url, CacheImageLoader.PaletteColor paletteColor) {
//        ContentValues colorValues = new ContentValues();
//        colorValues.put(PaletteColorEntry.COLUMN_NAME_IMAGE_URL, url);
//        colorValues.put(PaletteColorEntry.COLUMN_NAME_PALETTE_COLOR, paletteColor.getPaletteColor());
//        colorValues.put(PaletteColorEntry.COLUMN_NAME_IS_FETCHED, paletteColor.isFetched() ? 0 : 1);
//
//        mDatabase.delete(PaletteColorEntry.TABLE_NAME,
//                PaletteColorEntry.COLUMN_NAME_IMAGE_URL + "=?", new String[]{ url });
//        mDatabase.insert(PaletteColorEntry.TABLE_NAME, null, colorValues);
//    }

    public PaletteColor loadPaletteColor(int newsFeedPosition, String guid) {
        PaletteColor paletteColor = null;
        mDatabase.beginTransaction();
        try {
            paletteColor = loadVibrantColorInternal(newsFeedPosition, guid);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }

        return paletteColor;
    }

    private PaletteColor loadVibrantColorInternal(int newsFeedPosition, String guid) {
        Cursor colorsCursor = mDatabase.query(
                PaletteColorEntry.TABLE_NAME,
                null,
                PaletteColorEntry.COLUMN_NAME_FEED_POSITION + "=? and " +
                        PaletteColorEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ String.valueOf(newsFeedPosition), guid },
                null, null, null);
        colorsCursor.moveToFirst();

        PaletteColor paletteColor = null;
        if (!colorsCursor.isAfterLast()) {
            int vibrantColor = colorsCursor.getInt(
                    colorsCursor.getColumnIndex(PaletteColorEntry.COLUMN_NAME_PALETTE_COLOR));
            int typeOrdinal = colorsCursor.getInt(
                    colorsCursor.getColumnIndex(PaletteColorEntry.COLUMN_NAME_TYPE));
            PaletteColor.TYPE type = PaletteColor.TYPE.values()[typeOrdinal];
            paletteColor = new PaletteColor(vibrantColor, type);
        }

        colorsCursor.close();

        return paletteColor;
    }

    private NewsContent queryNewsContentByGuid(String guid) {
        Cursor newsContentCursor = mDatabase.query(
                NewsContentEntry.TABLE_NAME,
                null,
                NewsContentEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ guid },
                null, null, null);
        newsContentCursor.moveToFirst();

        if (newsContentCursor.getCount() <= 0) {
            newsContentCursor.close();
            // no saved news feed
            return NewsContent.createEmptyObject();
        }
        String title = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_TITLE));
        String text = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_TEXT));
        String url = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_URL));
        String videoUrl = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_VIDEO_URL));
        int fetchStateIndex = newsContentCursor.getInt(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_FETCH_STATE));
        NewsContentFetchState fetchState = NewsContentFetchState.getByIndex(fetchStateIndex);
        NewsContent newsContent = new NewsContent.Builder()
                .setTitle(title)
                .setText(text)
                .setUrl(url)
                .setVideoUrl(videoUrl)
                .setFetchState(fetchState)
                .build();

        newsContentCursor.close();

        return newsContent;
    }

    private void insertNewsImage(String imageUrl, int newsFeedPosition, String guid) {
        mDatabase.beginTransaction();
        try {
            insertNewsImageInternal(imageUrl, newsFeedPosition, guid);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    private void insertNewsImageInternal(String imageUrl, int newsFeedPosition, String guid) {
        ContentValues newsValues = new ContentValues();
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL, imageUrl);
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED, true);

        mDatabase.update(
                NewsEntry.TABLE_NAME,
                newsValues,
                NewsEntry.COLUMN_NAME_FEED_POSITION + "=? and " +
                        NewsEntry.COLUMN_NAME_GUID + "=?",
                new String[]{String.valueOf(newsFeedPosition), guid});
    }

    public void insertNewsImageFetchStateWithGuid(int state, int newsFeedPosition, String guid) {
        mDatabase.beginTransaction();
        try {
            insertNewsImageUrlFetchStateInternal(state, newsFeedPosition, guid);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    private void insertNewsImageUrlFetchStateInternal(int state, int newsFeedPosition, String guid) {
        ContentValues newsValues = new ContentValues();
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_STATE, state);

        mDatabase.update(
                NewsEntry.TABLE_NAME,
                newsValues,
                NewsEntry.COLUMN_NAME_FEED_POSITION + "=? and " +
                        NewsEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ String.valueOf(newsFeedPosition), guid });
    }

    private void insertNewsFeed(NewsFeed newsFeed, int newsFeedIndex) {
        mDatabase.beginTransaction();
        try {
            insertNewsFeedInternal(newsFeed, newsFeedIndex);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    private void insertNewsFeedInternal(NewsFeed newsFeed, int newsFeedIndex) {
        String newsFeedIndexStr = String.valueOf(newsFeedIndex);

        ContentValues newsFeedValues = new ContentValues();
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_POSITION, newsFeedIndex);
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TITLE, newsFeed.getTitle());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FEED_URL, newsFeed.getNewsFeedUrl().getUrl());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY,
                newsFeed.getNewsFeedUrl().getType().getUniqueKey());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FETCH_STATE_KEY,
                newsFeed.getNewsFeedFetchState().getKey());

        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_LANGUAGE_CODE, newsFeed.getTopicLanguageCode());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_REGION_CODE, newsFeed.getTopicRegionCode());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_COUNTRY_CODE, newsFeed.getTopicCountryCode());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_PROVIDER_ID, newsFeed.getTopicProviderId());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_ID, newsFeed.getTopicId());

        mDatabase.delete(NewsFeedEntry.TABLE_NAME, NewsFeedEntry.COLUMN_NAME_POSITION + "=?",
                new String[]{ newsFeedIndexStr });
        mDatabase.insert(NewsFeedEntry.TABLE_NAME, null, newsFeedValues);

        // insert news list
        mDatabase.delete(NewsEntry.TABLE_NAME, NewsEntry.COLUMN_NAME_FEED_POSITION + "=?",
                new String[]{ newsFeedIndexStr });
        mDatabase.delete(PaletteColorEntry.TABLE_NAME, PaletteColorEntry.COLUMN_NAME_FEED_POSITION + "=?",
                new String[]{ newsFeedIndexStr });
        ArrayList<News> newsList = newsFeed.getNewsList();
        if (newsList != null && newsList.size() > 0) {
            for (int newsIdx = 0; newsIdx < newsList.size(); newsIdx++) {
                News news = newsList.get(newsIdx);

                ContentValues newsValues = new ContentValues();
                newsValues.put(NewsEntry.COLUMN_NAME_FEED_POSITION, newsFeedIndex);
                newsValues.put(NewsEntry.COLUMN_NAME_INDEX, newsIdx);
                newsValues.put(NewsEntry.COLUMN_NAME_TITLE, news.getTitle());
                newsValues.put(NewsEntry.COLUMN_NAME_LINK, news.getLink());
                newsValues.put(NewsEntry.COLUMN_NAME_GUID, news.getGuid());
                newsValues.put(NewsEntry.COLUMN_NAME_PUB_DATE, news.getPubDateInMillis());
                newsValues.put(NewsEntry.COLUMN_NAME_DESCRIPTION, news.getDescription());
                newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL, news.getImageUrl());
                newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED, news.isImageUrlChecked());
                newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_STATE, news.getImageUrlState());

                mDatabase.insert(NewsEntry.TABLE_NAME, null, newsValues);

                saveNewsContentWithGuid(news);
            }
        }
    }

    private NewsFeed queryNewsFeed(int newsFeedPosition, boolean shuffle) {
        NewsFeed newsFeed = null;
        mDatabase.beginTransaction();
        try {
            newsFeed = queryNewsFeedInternal(newsFeedPosition, shuffle);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }

        return newsFeed;
    }

    private NewsFeed queryNewsFeedInternal(int newsFeedPosition, boolean shuffle) {
        String[] newsFeedWhereArgs = { String.valueOf(newsFeedPosition) };

        Cursor newsFeedCursor = mDatabase.query(
                NewsFeedEntry.TABLE_NAME,
                null,
                NewsFeedEntry.COLUMN_NAME_POSITION + "=?",
                newsFeedWhereArgs,
                null, null, null);
        newsFeedCursor.moveToFirst();

        if (newsFeedCursor.getCount() <= 0) {
            // no saved news feed
            newsFeedCursor.close();
            return null;
        }
        NewsFeed newsFeed = convertCursorToNewsFeed(newsFeedCursor);
        newsFeed.setNewsList(queryNewsListByNewsFeedPosition(newsFeedPosition, shuffle));

        newsFeedCursor.close();

        return newsFeed;
    }

    private NewsFeed convertCursorToNewsFeed(Cursor newsFeedCursor) {
        String title = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TITLE));
        String newsFeedUrl = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_FEED_URL));
        int newsFeedUrlTypeKey = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY));
        int newsFeedFetchStateKey = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_FETCH_STATE_KEY));

        String topicRegionCode = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_REGION_CODE));
        String topicLanguageCode = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_LANGUAGE_CODE));
        String topicCountryCode = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_COUNTRY_CODE));
        int topicProviderId = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_PROVIDER_ID));
        int topicId = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_ID));

        NewsFeed newsFeed = new NewsFeed();
        newsFeed.setTitle(title);
        newsFeed.setNewsFeedUrl(new NewsFeedUrl(newsFeedUrl, newsFeedUrlTypeKey));
        newsFeed.setDisplayingNewsIndex(0);
        newsFeed.setNewsFeedFetchState(NewsFeedFetchState.getByKey(newsFeedFetchStateKey));
        newsFeed.setTopicLanguageCode(topicLanguageCode);
        newsFeed.setTopicRegionCode(topicRegionCode);
        newsFeed.setTopicCountryCode(topicCountryCode);
        newsFeed.setTopicProviderId(topicProviderId);
        newsFeed.setTopicId(topicId);

        return newsFeed;
    }

    private ArrayList<News> queryNewsListByNewsFeedPosition(int newsFeedPosition, boolean shuffle) {
        ArrayList<News> newsList = new ArrayList<>();
        mDatabase.beginTransaction();
        try {
            newsList = queryNewsListByNewsFeedPositionInternal(newsFeedPosition, shuffle);
            mDatabase.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            mDatabase.endTransaction();
        }

        return newsList;
    }

    private ArrayList<News> queryNewsListByNewsFeedPositionInternal(
            int newsFeedPosition, boolean shuffle) {
        String[] newsListWhereArgs = { String.valueOf(newsFeedPosition) };

        Cursor newsListCursor = mDatabase.query(
                NewsEntry.TABLE_NAME,
                null,
                NewsEntry.COLUMN_NAME_FEED_POSITION + "=?",
                newsListWhereArgs,
                null, null, NewsEntry.COLUMN_NAME_INDEX);
        newsListCursor.moveToFirst();

        ArrayList<News> newsList = new ArrayList<>();
        while (!newsListCursor.isAfterLast()) {
            String newsTitle = newsListCursor.getString(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_TITLE));
            String newsLink = newsListCursor.getString(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_LINK));
            String guid = newsListCursor.getString(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_GUID));
            long pubDateInMillis = newsListCursor.getLong(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_PUB_DATE));
            String newsDescription = newsListCursor.getString(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_DESCRIPTION));
            NewsContent newsContent = queryNewsContentByGuid(guid);
            String newsImageUrl = newsListCursor.getString(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_IMAGE_URL));
            int newsImageUrlCheckedInt = newsListCursor.getInt(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED));
            boolean newsImageUrlChecked = newsImageUrlCheckedInt == 1;
            int newsImageUrlState = newsListCursor.getInt(
                    newsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_IMAGE_URL_STATE));

            News news = new News();
            news.setTitle(newsTitle);
            news.setLink(newsLink);
            news.setGuid(guid);
            news.setPubDate(pubDateInMillis);
            news.setDescription(newsDescription);
            news.setImageUrl(newsImageUrl);
            news.setImageUrlChecked(newsImageUrlChecked);
            news.setImageUrlState(newsImageUrlState);
            news.setNewsContent(newsContent);

            newsList.add(news);

            newsListCursor.moveToNext();
        }

        newsListCursor.close();

        if (!DEBUG_BLOCK_SHUFFLE && shuffle) {
            Collections.shuffle(newsList); // 캐쉬된 뉴스들도 무조건 셔플
        }

        return newsList;
    }

    public void clearArchiveDebug() {
        mDatabase.execSQL("DELETE FROM " + NewsFeedEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + NewsEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + NewsContentEntry.TABLE_NAME);
    }

    public static void copyDbToExternalStorageWithPostfix(Context context, String postfix)
            throws ExternalStorage.ExternalStorageException {
        copyDbToExternalStorage(context, postfix);
    }

    public static void copyDbToExternalStorage(Context context) throws ExternalStorage.ExternalStorageException {
        copyDbToExternalStorage(context, "");
    }

    private static void copyDbToExternalStorage(Context context, String postfix) throws ExternalStorage.ExternalStorageException {
        String dbFilePath = context.getDatabasePath(NewsDbHelper.DB_NAME_WITH_EXTENSION).toString();
        String outputFileName;
        if (postfix.length() > 0) {
            outputFileName = NewsDbHelper.DB_NAME + " " + postfix + NewsDbHelper.DB_EXTENSION;
        } else {
            outputFileName = NewsDbHelper.DB_NAME_WITH_EXTENSION;
        }

        File outputFile = ExternalStorage.createFileInExternalDirectory(outputFileName);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(dbFilePath);
            out = new FileOutputStream(outputFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
