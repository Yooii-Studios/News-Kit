package com.yooiistudios.news.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.util.NewsFeedUtils;
import com.yooiistudios.news.ui.widget.MainBottomContainerLayout;

import java.util.ArrayList;
import java.util.Collections;

import static com.yooiistudios.news.model.database.NewsDbContract.NewsEntry;
import static com.yooiistudios.news.model.database.NewsDbContract.NewsFeedEntry;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_KEY;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_SHARED_PREFERENCES;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 4.
 *
 * NewsDb
 *  쿼리, 삽입, 삭제, 업데이트 기능 래핑
 */
public class NewsDb {
    private static final String TAG = "NewsDB";
    private static final int TOP_NEWS_FEED_INDEX = -1;
    private static final int BOTTOM_NEWS_FEED_INITIAL_INDEX = 0;

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

    private void close() {
        if (isOpen()) {
            mDatabase.close();
        }
        if (mHelper != null) {
            mHelper.close();
        }
    }

    private boolean isOpen() {
        return mDatabase != null && mDatabase.isOpen();
    }

    private void checkDBStatus() {
        if (!isOpen()) {
            throw new IllegalStateException("You MUST open database with NewsArchiveDB.open() " +
                    "before database operations.");
        }
    }

    public NewsFeed loadTopNewsFeed(Context context) {
        return loadTopNewsFeed(context, true);
    }

    public NewsFeed loadTopNewsFeed(Context context, boolean shuffle) {
        NewsFeed newsFeed = queryNewsFeed(TOP_NEWS_FEED_INDEX, shuffle);
        if (newsFeed == null) {
            return NewsFeedUtils.getDefaultTopNewsFeed(context);
        } else {
            return newsFeed;
        }
    }

    public ArrayList<NewsFeed> loadBottomNewsFeedList(Context context) {
        return loadBottomNewsFeedList(context, true);
    }
    public ArrayList<NewsFeed> loadBottomNewsFeedList(Context context, boolean shuffle) {
        SharedPreferences prefs = context.getSharedPreferences(
                PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int panelMatrixUniqueKey = prefs.getInt(PANEL_MATRIX_KEY, MainBottomContainerLayout.PANEL_MATRIX.getDefault().uniqueKey);
        MainBottomContainerLayout.PANEL_MATRIX panelMatrix = MainBottomContainerLayout.PANEL_MATRIX.getByUniqueKey(panelMatrixUniqueKey);

        String[] newsFeedWhereArgs = {
                String.valueOf(BOTTOM_NEWS_FEED_INITIAL_INDEX),
                String.valueOf(panelMatrix.panelCount)
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
            ArrayList<NewsFeed> defaultNewsFeedList =
                    NewsFeedUtils.getDefaultBottomNewsFeedList(context);
            saveBottomNewsFeedList(defaultNewsFeedList);
            return defaultNewsFeedList;
        }
        while (!newsFeedCursor.isAfterLast()) {
            NewsFeed newsFeed = cursorToNewsFeed(newsFeedCursor, shuffle);

            int newsFeedPosition = newsFeedCursor.getInt(
                    newsFeedCursor.getColumnIndex(NewsFeedEntry.COLUMN_NAME_POSITION));
            newsFeed.setNewsList(queryNewsFromNewsFeedPosition(newsFeedPosition));

            newsFeed.setDisplayingNewsIndex(0);
            if (shuffle && newsFeed.getNewsList() != null && newsFeed.getNewsList().size() > 0) {
                Collections.shuffle(newsFeed.getNewsList()); // 캐쉬된 뉴스들도 무조건 셔플
            }

            newsFeedList.add(newsFeed);

            newsFeedCursor.moveToNext();
        }

        newsFeedCursor.close();

        int newsFeedCount = newsFeedList.size();
        if (newsFeedCount > panelMatrix.panelCount) {
            newsFeedList = new ArrayList<>(newsFeedList.subList(0, panelMatrix.panelCount));
        } else if (newsFeedCount < panelMatrix.panelCount) {
            ArrayList<NewsFeed> defaultNewsFeedList = NewsFeedUtils.getDefaultBottomNewsFeedList(context);
            for (int idx = newsFeedCount; idx < panelMatrix.panelCount; idx++) {
                newsFeedList.add(defaultNewsFeedList.get(idx));
            }
        }

        return newsFeedList;
    }

    public NewsFeed loadBottomNewsFeedAt(Context context, int position, boolean shuffle) {
        NewsFeed newsFeed = queryNewsFeed(position, shuffle);

        if (newsFeed == null) {
            // cache된 내용 없는 경우. 새로 만들어서 리턴.
            ArrayList<NewsFeed> newsFeedList = NewsFeedUtils.getDefaultBottomNewsFeedList(context);
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
        insertNewsFeed(newsFeed, TOP_NEWS_FEED_INDEX);
    }

    public void saveBottomNewsFeedList(ArrayList<NewsFeed> bottomNewsFeedList) {
        for (int idx = 0; idx < bottomNewsFeedList.size(); idx++) {
            insertNewsFeed(bottomNewsFeedList.get(idx), idx);
        }
    }

    public void saveBottomNewsFeedAt(NewsFeed bottomNewsFeed, int position) {
        insertNewsFeed(bottomNewsFeed, position);
    }

    public void saveTopNewsImageUrl(String imageUrl, boolean imageUrlChecked, int newsPosition) {
        insertNewsImage(imageUrl, imageUrlChecked, TOP_NEWS_FEED_INDEX, newsPosition);
    }

//    public void saveBottomNewsImageUrl(String imageUrl, boolean imageUrlChecked,
//                                       int newsFeedPosition, int newsPosition) {
//        insertNewsImage(imageUrl, imageUrlChecked, newsFeedPosition, newsPosition);
//    }

    private void insertNewsImage(String imageUrl, boolean imageUrlChecked,
                                 int newsFeedPosition, int newsPosition) {
        ContentValues newsValues = new ContentValues();
        newsValues.put(NewsEntry.COLUMN_NAME_FEED_POSITION, newsFeedPosition);
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL, imageUrl);
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED, imageUrlChecked);

        mDatabase.update(
                NewsEntry.TABLE_NAME,
                newsValues,
                NewsEntry.COLUMN_NAME_FEED_POSITION + "=? and " +
                        NewsEntry.COLUMN_NAME_INDEX + "=?",
                new String[]{ String.valueOf(newsFeedPosition), String.valueOf(newsPosition) });
    }

    private void insertNewsFeed(NewsFeed newsFeed, int newsFeedIndex) {
        String newsFeedIndexStr = String.valueOf(newsFeedIndex);

        ContentValues newsFeedValues = new ContentValues();
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_POSITION, newsFeedIndex);
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TITLE, newsFeed.getTitle());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FEED_URL, newsFeed.getNewsFeedUrl().getUrl());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY,
                newsFeed.getNewsFeedUrl().getType().getUniqueKey());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_IS_VALID, newsFeed.isValid());

        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_REGION_CODE, newsFeed.getTopicRegionCode());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_LANGUAGE_CODE, newsFeed.getTopicLanguageCode());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_PROVIDER_ID, newsFeed.getTopicProviderId());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TOPIC_ID, newsFeed.getTopicId());

        mDatabase.delete(NewsFeedEntry.TABLE_NAME, NewsFeedEntry.COLUMN_NAME_POSITION + "=?",
                new String[]{ newsFeedIndexStr });
        mDatabase.insert(NewsFeedEntry.TABLE_NAME, null, newsFeedValues);

        // insert news list

        mDatabase.delete(NewsEntry.TABLE_NAME, NewsEntry.COLUMN_NAME_FEED_POSITION + "=?",
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
                newsValues.put(NewsEntry.COLUMN_NAME_DESCRIPTION, news.getDescription());
                newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL, news.getImageUrl());
                newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED, news.isImageUrlChecked());

                mDatabase.insert(NewsEntry.TABLE_NAME, null, newsValues);
            }
        }
    }

    private NewsFeed queryNewsFeed(int newsFeedPosition, boolean shuffle) {
        String[] newsFeedWhereArgs = { String.valueOf(newsFeedPosition) };

        Cursor newsFeedCursor = mDatabase.query(
                NewsFeedEntry.TABLE_NAME,
                null,
                NewsFeedEntry.COLUMN_NAME_POSITION + "=?",
                newsFeedWhereArgs,
                null, null, null);
        newsFeedCursor.moveToFirst();

        if (newsFeedCursor.getCount() <= 0) {
            // no saved top news feed
            return null;
        }
        NewsFeed newsFeed = cursorToNewsFeed(newsFeedCursor, shuffle);
        newsFeed.setNewsList(queryNewsFromNewsFeedPosition(newsFeedPosition));

        newsFeedCursor.close();

        return newsFeed;
    }

    private NewsFeed cursorToNewsFeed(Cursor newsFeedCursor, boolean shuffle) {
        String title = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TITLE));
        String newsFeedUrl = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_FEED_URL));
        int newsFeedUrlTypeKey = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY));
        int isValidInt = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_IS_VALID));
        boolean isValid = isValidInt == 1;

        String topicRegionCode = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_REGION_CODE));
        String topicLanguageCode = newsFeedCursor.getString(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_LANGUAGE_CODE));
        int topicProviderId = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_PROVIDER_ID));
        int topicId = newsFeedCursor.getInt(newsFeedCursor.getColumnIndex(
                NewsFeedEntry.COLUMN_NAME_TOPIC_ID));

        NewsFeed newsFeed = new NewsFeed();
        newsFeed.setTitle(title);
        newsFeed.setNewsFeedUrl(new NewsFeedUrl(newsFeedUrl, newsFeedUrlTypeKey));
        newsFeed.setDisplayingNewsIndex(0);
        newsFeed.setValid(isValid);
        newsFeed.setTopicRegionCode(topicRegionCode);
        newsFeed.setTopicLanguageCode(topicLanguageCode);
        newsFeed.setTopicProviderId(topicProviderId);
        newsFeed.setTopicId(topicId);

        if (shuffle) {
            Collections.shuffle(newsFeed.getNewsList()); // 캐쉬된 뉴스들도 무조건 셔플
        }

        return newsFeed;
    }

    private ArrayList<News> queryNewsFromNewsFeedPosition(int newsFeedPosition) {
        String[] newsListWhereArgs = { String.valueOf(newsFeedPosition) };

        Cursor topNewsListCursor = mDatabase.query(
                NewsEntry.TABLE_NAME,
                null,
                NewsEntry.COLUMN_NAME_FEED_POSITION + "=?",
                newsListWhereArgs,
                null, null, NewsEntry.COLUMN_NAME_INDEX);
        topNewsListCursor.moveToFirst();

        ArrayList<News> newsList = new ArrayList<>();
        while (!topNewsListCursor.isAfterLast()) {
            String newsTitle = topNewsListCursor.getString(
                    topNewsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_TITLE));
            String newsLink = topNewsListCursor.getString(
                    topNewsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_LINK));
            String newsDescription = topNewsListCursor.getString(
                    topNewsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_DESCRIPTION));
            String newsImageUrl = topNewsListCursor.getString(
                    topNewsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_IMAGE_URL));
            int newsImageUrlCheckedInt = topNewsListCursor.getInt(
                    topNewsListCursor.getColumnIndex(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED));
            boolean newsImageUrlChecked = newsImageUrlCheckedInt == 1;

            News news = new News();
            news.setTitle(newsTitle);
            news.setLink(newsLink);
            news.setDescription(newsDescription);
            news.setImageUrl(newsImageUrl);
            news.setImageUrlChecked(newsImageUrlChecked);

            newsList.add(news);

            topNewsListCursor.moveToNext();
        }

        topNewsListCursor.close();

        return newsList;
    }

    public void clearArchive() {
        mDatabase.execSQL("DELETE FROM " + NewsFeedEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + NewsEntry.TABLE_NAME);
//        mDatabase.execSQL(NewsDbHelper.SQL_DROP_NEWSFEED_ENTRY);
//        mDatabase.execSQL(NewsDbHelper.SQL_DROP_NEWS_ENTRY);
    }
}
