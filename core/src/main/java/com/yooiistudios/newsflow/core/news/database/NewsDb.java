package com.yooiistudios.newsflow.core.news.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.yooiistudios.newsflow.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContentFetchState;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedUrl;
import com.yooiistudios.newsflow.core.util.ExternalStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import static com.yooiistudios.newsflow.core.news.database.NewsDbContract.NewsContentEntry;
import static com.yooiistudios.newsflow.core.news.database.NewsDbContract.NewsEntry;
import static com.yooiistudios.newsflow.core.news.database.NewsDbContract.NewsFeedEntry;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 4.
 *
 * NewsDb
 *  쿼리, 삽입, 삭제, 업데이트 기능 래핑
 */
public class NewsDb {
    private static final int TOP_NEWS_FEED_INDEX = -1;
    private static final int BOTTOM_NEWS_FEED_INITIAL_INDEX = 0;
    private static final boolean DEBUG_BLOCK_SHUFFLE = true;

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
        NewsFeed newsFeed = queryNewsFeed(TOP_NEWS_FEED_INDEX, shuffle);
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
        // TODO Transaction
        String[] newsFeedWhereArgs = {
                String.valueOf(BOTTOM_NEWS_FEED_INITIAL_INDEX),
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

    public void saveNewsContentWithGuid(News news) {
        // TODO Transaction
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
//        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_IMAGE_URL, newsContent.getImageUrl());
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_VIDEO_URL, newsContent.getVideoUrl());
        newsFeedValues.put(NewsContentEntry.COLUMN_NAME_FETCH_STATE, newsContent.getFetchState().getIndex());

        mDatabase.delete(NewsContentEntry.TABLE_NAME, NewsContentEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ guid });
        mDatabase.insert(NewsContentEntry.TABLE_NAME, null, newsFeedValues);

        // insert news list
//        mDatabase.delete(NewsContentTextEntry.TABLE_NAME, NewsContentTextEntry.COLUMN_NAME_GUID + "=?",
//                new String[]{ guid });
//        mDatabase.delete(NewsContentImageEntry.TABLE_NAME, NewsContentImageEntry.COLUMN_NAME_GUID + "=?",
//                new String[]{ guid });
//        saveNewsContentTextsWithGuid(guid, newsContent.getTextList());
//        saveNewsContentImagesWithGuid(guid, newsContent.getImages());
    }

    public void saveTopNewsImageUrlWithGuid(String imageUrl, String guid) {
        insertNewsImage(imageUrl, TOP_NEWS_FEED_INDEX, guid);
    }

    public void saveBottomNewsImageUrlWithGuid(String imageUrl,
                                               int newsFeedPosition, String guid) {
        insertNewsImage(imageUrl, newsFeedPosition, guid);
    }

//    private void saveNewsContentTextsWithGuid(String guid, List<String> textList) {
//        for (int i = 0; i < textList.size(); i++) {
//            String text = textList.get(i);
//
//            ContentValues newsValues = new ContentValues();
//            newsValues.put(NewsContentTextEntry.COLUMN_NAME_INDEX, i);
//            newsValues.put(NewsContentTextEntry.COLUMN_NAME_GUID, guid);
//            newsValues.put(NewsContentTextEntry.COLUMN_NAME_TEXT, text);
//
//            mDatabase.insert(NewsContentTextEntry.TABLE_NAME, null, newsValues);
//        }
//    }

//    private void saveNewsContentImagesWithGuid(String guid, List<NewsContentImage> images) {
//        for (int i = 0; i < images.size(); i++) {
//            NewsContentImage image = images.get(i);
//
//            ContentValues newsValues = new ContentValues();
//            newsValues.put(NewsContentImageEntry.COLUMN_NAME_INDEX, i);
//            newsValues.put(NewsContentImageEntry.COLUMN_NAME_GUID, guid);
//            newsValues.put(NewsContentImageEntry.COLUMN_NAME_IMAGE_URL, image.getImageUrl());
//            newsValues.put(NewsContentImageEntry.COLUMN_NAME_WEIGHT, image.getWeight());
//            newsValues.put(NewsContentImageEntry.COLUMN_NAME_WIDTH, image.getWidth());
//            newsValues.put(NewsContentImageEntry.COLUMN_NAME_HEIGHT, image.getHeight());
//
//            mDatabase.insert(NewsContentImageEntry.TABLE_NAME, null, newsValues);
//        }
//    }

    private NewsContent queryNewsContentByGuid(String guid) {
        Cursor newsContentCursor = mDatabase.query(
                NewsContentEntry.TABLE_NAME,
                null,
                NewsContentEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ guid },
                null, null, null);
        newsContentCursor.moveToFirst();

        if (newsContentCursor.getCount() <= 0) {
            // no saved news feed
            return NewsContent.createEmptyObject();
        }
        String title = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_TITLE));
        String text = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_TEXT));
        String url = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_URL));
//        String imageUrl = newsContentCursor.getString(
//                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_IMAGE_URL));
        String videoUrl = newsContentCursor.getString(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_VIDEO_URL));
        int fetchStateIndex = newsContentCursor.getInt(
                newsContentCursor.getColumnIndex(NewsContentEntry.COLUMN_NAME_FETCH_STATE));
        NewsContentFetchState fetchState = NewsContentFetchState.getByIndex(fetchStateIndex);
        NewsContent newsContent = new NewsContent.Builder()
                .setTitle(title)
                .setText(text)
                .setUrl(url)
//                .setImageUrl(imageUrl)
                .setVideoUrl(videoUrl)
                .setFetchState(fetchState)
//                .setTexts(queryNewsContentTextsByGuid(guid))
//                .setImages(queryNewsContentImagesByGuid(guid))
                .build();

        newsContentCursor.close();

        return newsContent;
    }

//    private List<String> queryNewsContentTextsByGuid(String guid) {
//        Cursor newsContentTextsCursor = mDatabase.query(
//                NewsContentTextEntry.TABLE_NAME,
//                null,
//                NewsContentTextEntry.COLUMN_NAME_GUID + "=?",
//                new String[]{ guid },
//                null, null, NewsContentTextEntry.COLUMN_NAME_INDEX);
//        newsContentTextsCursor.moveToFirst();
//
//        List<String> texts = new ArrayList<>();
//        while (!newsContentTextsCursor.isAfterLast()) {
//            String text = newsContentTextsCursor.getString(
//                    newsContentTextsCursor.getColumnIndex(NewsContentTextEntry.COLUMN_NAME_TEXT));
//            texts.add(text);
//        }
//
//        newsContentTextsCursor.close();
//
//        return texts;
//    }

//    private List<NewsContentImage> queryNewsContentImagesByGuid(String guid) {
//        Cursor newsContentImagesCursor = mDatabase.query(
//                NewsContentImageEntry.TABLE_NAME,
//                null,
//                NewsContentImageEntry.COLUMN_NAME_GUID + "=?",
//                new String[]{ guid },
//                null, null, NewsContentImageEntry.COLUMN_NAME_INDEX);
//        newsContentImagesCursor.moveToFirst();
//
//        List<NewsContentImage> images = new ArrayList<>();
//        while (!newsContentImagesCursor.isAfterLast()) {
//            String url = newsContentImagesCursor.getString(
//                    newsContentImagesCursor.getColumnIndex(NewsContentImageEntry.COLUMN_NAME_IMAGE_URL));
//            int weight = newsContentImagesCursor.getInt(
//                    newsContentImagesCursor.getColumnIndex(NewsContentImageEntry.COLUMN_NAME_WEIGHT));
//            int width = newsContentImagesCursor.getInt(
//                    newsContentImagesCursor.getColumnIndex(NewsContentImageEntry.COLUMN_NAME_WIDTH));
//            int height = newsContentImagesCursor.getInt(
//                    newsContentImagesCursor.getColumnIndex(NewsContentImageEntry.COLUMN_NAME_HEIGHT));
//
//            NewsContentImage image = new NewsContentImage(url, weight, width, height);
//            images.add(image);
//        }
//
//        newsContentImagesCursor.close();
//
//        return images;
//    }

    private void insertNewsImage(String imageUrl,
                                 int newsFeedPosition, String guid) {
        ContentValues newsValues = new ContentValues();
//        newsValues.put(NewsEntry.COLUMN_NAME_FEED_POSITION, newsFeedPosition);
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL, imageUrl);
        newsValues.put(NewsEntry.COLUMN_NAME_IMAGE_URL_CHECKED, true);

        mDatabase.update(
                NewsEntry.TABLE_NAME,
                newsValues,
                NewsEntry.COLUMN_NAME_FEED_POSITION + "=? and " +
                        NewsEntry.COLUMN_NAME_GUID + "=?",
                new String[]{ String.valueOf(newsFeedPosition), String.valueOf(guid) });
    }

    private void insertNewsFeed(NewsFeed newsFeed, int newsFeedIndex) {
        // TODO Transaction
        String newsFeedIndexStr = String.valueOf(newsFeedIndex);

        ContentValues newsFeedValues = new ContentValues();
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_POSITION, newsFeedIndex);
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_TITLE, newsFeed.getTitle());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FEED_URL, newsFeed.getNewsFeedUrl().getUrl());
        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_FEED_URL_TYPE_KEY,
                newsFeed.getNewsFeedUrl().getType().getUniqueKey());
//        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_IS_VALID, newsFeed.containsNews());
//        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_LINK, newsFeed.getLink());
//        newsFeedValues.put(NewsFeedEntry.COLUMN_NAME_DESCRIPTION, newsFeed.getDescription());

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

                mDatabase.insert(NewsEntry.TABLE_NAME, null, newsValues);

                saveNewsContentWithGuid(news);
            }
        }
    }

    private NewsFeed queryNewsFeed(int newsFeedPosition, boolean shuffle) {
        // TODO Transaction
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
        newsFeed.setTopicLanguageCode(topicLanguageCode);
        newsFeed.setTopicRegionCode(topicRegionCode);
        newsFeed.setTopicCountryCode(topicCountryCode);
        newsFeed.setTopicProviderId(topicProviderId);
        newsFeed.setTopicId(topicId);

        return newsFeed;
    }

    private ArrayList<News> queryNewsListByNewsFeedPosition(int newsFeedPosition, boolean shuffle) {
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

            News news = new News();
            news.setTitle(newsTitle);
            news.setLink(newsLink);
            news.setGuid(guid);
            news.setPubDate(pubDateInMillis);
            news.setDescription(newsDescription);
            news.setImageUrl(newsImageUrl);
            news.setImageUrlChecked(newsImageUrlChecked);
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

    public void clearArchive() {
        mDatabase.execSQL("DELETE FROM " + NewsFeedEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + NewsEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + NewsContentEntry.TABLE_NAME);
    }

    public static void copyDbToExternalStorage(Context context) {
        String dbFilePath = context.getDatabasePath(NewsDbHelper.DB_NAME).toString();
        File outputFile = ExternalStorage.createFileInExternalDirectory(context,
                NewsDbHelper.DB_NAME);

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
