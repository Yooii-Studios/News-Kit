package com.yooiistudios.newskit;

import android.content.Context;
import android.test.AndroidTestCase;

import com.yooiistudios.newskit.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.NewsFeedUrlType;
import com.yooiistudios.newskit.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newskit.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newskit.core.news.database.NewsDb;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 5.
 *
 * DbTest
 *  데이터베이스 테스트를 위한 클래스
 */
public class DatabaseTest extends AndroidTestCase {
//    private Activity mActivity;
    private Context mContext;
//    private Instrumentation mInstrumentation;

    public DatabaseTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        mActivity = getActivity();
        mContext = getContext();
//        mInstrumentation = getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private static ArrayList<NewsFeed> makeDummyNewsFeedList() {
        ArrayList<NewsFeed> dummyNewsFeedList = new ArrayList<>();

        for (int newsFeedIndex = 0; newsFeedIndex < 8; newsFeedIndex++) {
            NewsFeed dummyNewsFeed = makeDummyNewsFeed(newsFeedIndex);

            dummyNewsFeedList.add(dummyNewsFeed);
        }

        return dummyNewsFeedList;
    }

    private static NewsFeed makeDummyNewsFeed(int newsFeedIndex) {
        NewsFeed dummyNewsFeed = new NewsFeed();
        dummyNewsFeed.setTitle("NewsFeed " + newsFeedIndex);
        NewsFeedUrlType urlType = NewsFeedUrlType.values()[newsFeedIndex % NewsFeedUrlType.values().length];
        dummyNewsFeed.setNewsFeedUrl(new NewsFeedUrl("NewsFeedUrl " + newsFeedIndex, urlType));
//            dummyNewsFeed.setValid(newsFeedIndex % 2 == 0);
        dummyNewsFeed.setDisplayingNewsIndex(newsFeedIndex%2);
        dummyNewsFeed.setTopicRegionCode("NewsFeed " + newsFeedIndex + " region code");
        dummyNewsFeed.setTopicLanguageCode("NewsFeed " + newsFeedIndex + " language code");
        dummyNewsFeed.setTopicProviderId(newsFeedIndex);
        dummyNewsFeed.setTopicId(newsFeedIndex);

        ArrayList<News> dummyNewsList = new ArrayList<>();

        for (int newsIndex = 0; newsIndex < 10; newsIndex++) {
            News dummyNews = new News();
            dummyNews.setTitle("News " + newsIndex);
            dummyNews.setLink("News link " + newsIndex);
            dummyNews.setGuid("News guid " + newsIndex);
            dummyNews.setDescription("News description " + newsIndex);
            if (newsIndex % 3 == 0) {
                dummyNews.setImageUrl("www.naver.com/img" + newsIndex);
                dummyNews.setImageUrlChecked(true);
            }

            dummyNewsList.add(dummyNews);
        }

        dummyNewsFeed.setNewsList(dummyNewsList);
        return dummyNewsFeed;
    }

//    private static NewsFeed makeDummyNewsFeed(int i) {
//        NewsFeed dummyNewsFeed = new NewsFeed();
//        dummyNewsFeed.setTitle("New York Times - Top Stories " + i);
//        dummyNewsFeed.setNewsFeedUrl(new NewsFeedUrl("http://sweetpjy.tistory.com/rss/",
//                NewsFeedUrlType.CUSTOM));
////        dummyNewsFeed.setValid(true);
//        dummyNewsFeed.setDisplayingNewsIndex(9); // default or > 0
//        dummyNewsFeed.setTopicRegionCode("some region code " + i);
//        dummyNewsFeed.setTopicLanguageCode("some language code " + i);
//        dummyNewsFeed.setTopicProviderId(i+1);
//        dummyNewsFeed.setTopicId((i+1) * 2);
//
//        ArrayList<News> dummyNewsList = new ArrayList<>();
//
//        News dummyNews1 = new News();
//        dummyNews1.setTitle("News 1 at" + i);
//        dummyNews1.setLink("http://sweetpjy.tistory.com/entry/2ch-야한-의미인-줄-알았던-단어-음란마귀주의");
//        dummyNews1.setDescription("some description " + i);
//        dummyNews1.setImageUrl("www.naver.com/index.png" + i);
//        dummyNews1.setImageUrlChecked(true);
//
//        dummyNewsList.add(dummyNews1);
//
//        News dummyNews2 = new News();
//        dummyNews2.setTitle("News 2 at" + i);
//        dummyNews2.setLink("http://sweetpjy.tistory.com/entry/2ch-야한-의미인-줄-알았던-단어-음란마귀주의");
//        dummyNews1.setDescription("some description " + i);
//
//        dummyNewsList.add(dummyNews2);
//
//        dummyNewsFeed.setNewsList(dummyNewsList);
//
//        return dummyNewsFeed;
//    }

    public void testTopNewsFeedInsertion() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        NewsFeed dummyNewsFeed = makeDummyNewsFeed(0);
        assertNotNull(dummyNewsFeed);

        NewsDb.getInstance(mContext).saveTopNewsFeed(dummyNewsFeed);
        NewsFeed loadedNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext, false);
        assertNotNull(loadedNewsFeed);
    }

    public void testTopNewsFeedQuery() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        // Retrieve from empty table
        NewsFeed loadedDefaultNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext, false);

        NewsFeed defaultNewsFeed = DefaultNewsFeedProvider.getDefaultTopNewsFeed(mContext);
        checkNewsFeedEquals(defaultNewsFeed, loadedDefaultNewsFeed);

        // Save dummy and retrieve.
        NewsFeed dummyNewsFeed = makeDummyNewsFeed(0);

        NewsDb.getInstance(mContext).saveTopNewsFeed(dummyNewsFeed);
        NewsFeed loadedNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext, false);

        checkNewsFeedEquals(dummyNewsFeed, loadedNewsFeed);
    }

    public void testTopNewsFeedUpdate() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        NewsFeed dummyNewsFeed = makeDummyNewsFeed(0);

        NewsDb.getInstance(mContext).saveTopNewsFeed(dummyNewsFeed);

        NewsFeed newNewsFeed = new NewsFeed();
        newNewsFeed.setTitle("CNN - Health");
        newNewsFeed.setNewsFeedUrl(new NewsFeedUrl("http://www.google.com/", NewsFeedUrlType.CUSTOM));
//        newNewsFeed.setValid(false);
        newNewsFeed.setDisplayingNewsIndex(0); // default or > 0
        newNewsFeed.setTopicRegionCode("some region code");
        newNewsFeed.setTopicLanguageCode("some language code");
        newNewsFeed.setTopicProviderId(345);
        newNewsFeed.setTopicId(456);
        newNewsFeed.setNewsList(null);

        NewsDb.getInstance(mContext).saveTopNewsFeed(newNewsFeed);
        NewsFeed loadedNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext, false);

        checkNewsFeedEquals(newNewsFeed, loadedNewsFeed);
    }

    public void testBottomNewsFeedInsertion() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        ArrayList<NewsFeed> dummyNewsFeedList = makeDummyNewsFeedList();
        assertNotNull(dummyNewsFeedList);

        NewsDb.getInstance(mContext).saveBottomNewsFeedList(dummyNewsFeedList);

        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());
        ArrayList<NewsFeed> loadedNewsFeed = NewsDb.getInstance(mContext).loadBottomNewsFeedList(
                mContext, currentMatrix.getPanelCount());
        assertNotNull(loadedNewsFeed);
    }

    public void testBottomNewsFeedQuery() {
        NewsDb.getInstance(mContext).clearArchiveDebug();
        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

        // Retrieve from empty table
        ArrayList<NewsFeed> loadedDefaultNewsFeedList = NewsDb.getInstance(mContext)
                .loadBottomNewsFeedList(mContext, currentMatrix.getPanelCount(), false);
        assertNotNull(loadedDefaultNewsFeedList);

        // Make default news feed list.
        ArrayList<NewsFeed> defaultNewsFeedList = DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(mContext);

        // Check if they have save news feed count.
        assertEquals(defaultNewsFeedList.size(), loadedDefaultNewsFeedList.size());

        // Check equality.
        for (int idx = 0; idx < loadedDefaultNewsFeedList.size(); idx++) {
            checkNewsFeedEquals(loadedDefaultNewsFeedList.get(idx), defaultNewsFeedList.get(idx));
        }

        NewsDb.getInstance(mContext).clearArchiveDebug();

        // Save dummy and retrieve.
        ArrayList<NewsFeed> dummyNewsFeedList = makeDummyNewsFeedList();

        NewsDb.getInstance(mContext).saveBottomNewsFeedList(dummyNewsFeedList);
        ArrayList<NewsFeed> loadedNewsFeedList = NewsDb.getInstance(mContext)
                .loadBottomNewsFeedList(mContext, currentMatrix.getPanelCount(), false);
        assertNotNull(loadedNewsFeedList);

        assertEquals(
                DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(mContext).size(),
                loadedNewsFeedList.size());
        int newsFeedCount = loadedNewsFeedList.size();

        for (int i = 0; i < newsFeedCount; i++) {
            checkNewsFeedEquals(dummyNewsFeedList.get(i), loadedNewsFeedList.get(i));
        }
    }

    public void testBottomNewsFeedUpdate() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        ArrayList<NewsFeed> dummyNewsFeedList = makeDummyNewsFeedList();
        NewsDb.getInstance(mContext).saveBottomNewsFeedList(dummyNewsFeedList);

        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());
        ArrayList<NewsFeed> loadedNewsFeedList = NewsDb.getInstance(mContext)
                .loadBottomNewsFeedList(mContext, currentMatrix.getPanelCount(), false);

        int targetNewsFeedIdx = loadedNewsFeedList.size() - 1;
        NewsFeed targetNewsFeed = loadedNewsFeedList.get(targetNewsFeedIdx);
        targetNewsFeed.setTitle("CNN - Health");
        targetNewsFeed.setNewsFeedUrl(new NewsFeedUrl("http://www.google.com/", NewsFeedUrlType.CUSTOM));
//        targetNewsFeed.setValid(false);
        targetNewsFeed.setDisplayingNewsIndex(0); // default or > 0
        targetNewsFeed.setTopicRegionCode("some region code");
        targetNewsFeed.setTopicLanguageCode("some language code");
        targetNewsFeed.setTopicProviderId(345);
        targetNewsFeed.setTopicId(456);
        targetNewsFeed.setNewsList(null);

        NewsDb.getInstance(mContext).saveBottomNewsFeedAt(targetNewsFeed, targetNewsFeedIdx);
        NewsFeed loadedNewsFeed = NewsDb.getInstance(mContext).loadBottomNewsFeedAt(
                mContext, targetNewsFeedIdx, false);

        checkNewsFeedEquals(targetNewsFeed, loadedNewsFeed);
    }

    public void testTopNewsImageUrlUpdate() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        NewsFeed dummyNewsFeed = makeDummyNewsFeed(0);
        NewsDb.getInstance(mContext).saveTopNewsFeed(dummyNewsFeed);

        NewsFeed initiallySavedNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext);

        int updateNewsIdx = initiallySavedNewsFeed.getNewsList().size() - 1;
        News newsToUpdate = initiallySavedNewsFeed.getNewsList().get(updateNewsIdx);

        // When news image is available
        String dummyImageUrl = "new news image url";
        NewsDb.getInstance(mContext).saveTopNewsImageUrlWithGuid(dummyImageUrl, newsToUpdate.getGuid());
        NewsFeed loadedNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext, false);

//        News actualNews = loadedNewsFeed.getNewsList().get(updateNewsIdx);
        News actualNews = loadedNewsFeed.getNewsByGuid(newsToUpdate.getGuid());

        String actualImageUrl = actualNews.getImageUrl();
        assertEquals(dummyImageUrl, actualImageUrl);

        boolean actualImageUrlChecked = actualNews.isImageUrlChecked();
        assertEquals(true, actualImageUrlChecked);
    }

    public void testBottomNewsImageUrlUpdate() {
        NewsDb.getInstance(mContext).clearArchiveDebug();

        ArrayList<NewsFeed> dummyNewsFeedList = makeDummyNewsFeedList();
        NewsDb.getInstance(mContext).saveBottomNewsFeedList(dummyNewsFeedList);

        int updateNewsFeedIdx = dummyNewsFeedList.size() - 1;

        NewsFeed initiallySavedNewsFeed = NewsDb.getInstance(mContext).loadBottomNewsFeedAt(
                mContext, updateNewsFeedIdx, true);

//        NewsFeed targetNewsFeed = dummyNewsFeedList.get(updateNewsFeedIdx);
        int updateNewsIdx = initiallySavedNewsFeed.getNewsList().size() - 1;
        News newsToUpdate = initiallySavedNewsFeed.getNewsList().get(updateNewsIdx);

        // When news image is available
        String dummyImageUrl = "new news image url";
        NewsDb.getInstance(mContext).saveBottomNewsImageUrlWithGuid(dummyImageUrl,
                updateNewsFeedIdx, newsToUpdate.getGuid());
        NewsFeed loadedNewsFeed = NewsDb.getInstance(mContext).loadBottomNewsFeedAt(mContext,
                updateNewsFeedIdx, false);

//        News actualNews = loadedNewsFeed.getNewsList().get(updateNewsIdx);
        News actualNews = loadedNewsFeed.getNewsByGuid(newsToUpdate.getGuid());

        String actualImageUrl = actualNews.getImageUrl();
        assertEquals(dummyImageUrl, actualImageUrl);

        boolean actualImageUrlChecked = actualNews.isImageUrlChecked();
        assertEquals(true, actualImageUrlChecked);
    }

    private void checkNewsFeedEquals(NewsFeed expectedNewsFeed, NewsFeed actualNewsFeed) {
        //NewsFeed property check
        assertEquals(expectedNewsFeed.getTitle(), actualNewsFeed.getTitle());
        assertEquals(expectedNewsFeed.getNewsFeedUrl().getUrl(), actualNewsFeed.getNewsFeedUrl().getUrl());
        assertEquals(expectedNewsFeed.getNewsFeedUrl().getType(), actualNewsFeed.getNewsFeedUrl().getType());
        assertEquals(expectedNewsFeed.containsNews(), actualNewsFeed.containsNews());
        // MUST BE 0 when loaded.(or, should not saved.)
        assertEquals(0, actualNewsFeed.getDisplayingNewsIndex());
        assertEquals(expectedNewsFeed.getTopicRegionCode(), actualNewsFeed.getTopicRegionCode());
        assertEquals(expectedNewsFeed.getTopicLanguageCode(), actualNewsFeed.getTopicLanguageCode());
        assertEquals(expectedNewsFeed.getTopicProviderId(), actualNewsFeed.getTopicProviderId());
        assertEquals(expectedNewsFeed.getTopicId(), actualNewsFeed.getTopicId());

        if (expectedNewsFeed.getNewsList() == null) {
            assertTrue(
                    actualNewsFeed.getNewsList() == null
                    || actualNewsFeed.getNewsList().size() == 0
            );
        } else {
            assertNotNull(actualNewsFeed.getNewsList());
            assertEquals(expectedNewsFeed.getNewsList().size(), actualNewsFeed.getNewsList().size());
            for (int newsIdx = 0; newsIdx < expectedNewsFeed.getNewsList().size(); newsIdx++) {
                News expectedNews = expectedNewsFeed.getNewsList().get(newsIdx);
                News actualNews = actualNewsFeed.getNewsList().get(newsIdx);
                assertEquals(expectedNews.getTitle(), actualNews.getTitle());
                assertEquals(expectedNews.getLink(), actualNews.getLink());
                assertEquals(expectedNews.getGuid(), actualNews.getGuid());
                assertEquals(expectedNews.getDescription(), actualNews.getDescription());
                assertEquals(expectedNews.getImageUrl(), actualNews.getImageUrl());
                assertEquals(expectedNews.isImageUrlChecked(), actualNews.isImageUrlChecked());
            }
        }
    }
}
