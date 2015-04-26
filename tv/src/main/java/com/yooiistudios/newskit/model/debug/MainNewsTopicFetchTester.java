package com.yooiistudios.newskit.model.debug;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.curation.NewsContentProvider;
import com.yooiistudios.newskit.core.news.database.NewsDb;
import com.yooiistudios.newskit.core.util.ExternalStorage;
import com.yooiistudios.newskit.core.util.NLLog;
import com.yooiistudios.newskit.ui.activity.MainActivity;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 16.
 *
 * MainNewsTopicFetchTest
 *  메인엑티비티 테스트용
 */
public class MainNewsTopicFetchTester {
    private MainActivity mActivity;

    private int mTopNewsCount;
    private SparseArray<Integer> mBottomNewsCount = new SparseArray<>();
    private boolean mTopFetched;
    private boolean mBottomAllFetched;

    public MainNewsTopicFetchTester(MainActivity activity) {
        mActivity = activity;
    }

    // DEBUG

    public void testFetch() {
        NewsDb.getInstance(mActivity).clearArchiveDebug();

        NewsFeed topNewsFeed = new NewsFeed(getDefaultTopic("en", null, "us", 1));
        ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("en", null, "us", 2)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("en", null, "us", 3)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("en", null, "us", 4)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("en", null, "uk", 1)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("en", null, "uk", 2)));

//        NewsFeed topNewsFeed = new NewsFeed(getDefaultTopic("fr", null, "fr", 1));
//        ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("fr", null, "fr", 2)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("fr", null, "fr", 3)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("fr", null, "ca", 1)));

//        NewsFeed topNewsFeed = new NewsFeed(getDefaultTopic("zh", "tw", "hk", 1));
//        ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("de", null, "de", 1)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("de", null, "de", 2)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("de", null, "de", 3)));

//        NewsFeed topNewsFeed = new NewsFeed(getDefaultTopic("ru", null, "ru", 1));
//        ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ru", null, "ru", 2)));

//        NewsFeed topNewsFeed = new NewsFeed(getDefaultTopic("ko", null, "kr", 1));
//        ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 2)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 3)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 4)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 5)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 6)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 7)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ko", null, "kr", 8)));

//        NewsFeed topNewsFeed = new NewsFeed(getDefaultTopic("ja", null, "jp", 1));
//        ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ja", null, "jp", 2)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ja", null, "jp", 3)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("ja", null, "jp", 4)));
//        bottomNewsFeeds.add(new NewsFeed(getDefaultTopic("sv", null, "se", 1)));

        mActivity.fetch(topNewsFeed, bottomNewsFeeds);
    }

    private NewsTopic getDefaultTopic(@NonNull String targetLanguageCode,
                                      @Nullable String targetRegionCode,
                                      @NonNull String targetCountryCode, int targetProviderId) {
        ArrayList<NewsTopic> topics =
                NewsContentProvider.getInstance(mActivity).getNewsProvider(
                        targetLanguageCode, targetRegionCode, targetCountryCode, targetProviderId)
                        .getNewsTopicList();

        NewsTopic targetTopic = null;
        for (NewsTopic topic : topics) {
            if (topic.isDefault()) {
                targetTopic = topic;
                break;
            }
        }

        return targetTopic;
    }

    public void checkAllTopNewsContentFetched() {
        mTopNewsCount -= 1;
        if (mTopNewsCount == 0) {
            NLLog.now("Top NewsContent fetch done.");
            mTopFetched = true;
            saveDebug();
        }
    }

    public void checkAllBottomNewsContentFetched(int newsFeedPosition) {
        int prevNewsCount = mBottomNewsCount.get(newsFeedPosition);
        mBottomNewsCount.put(newsFeedPosition, --prevNewsCount);
        boolean allFetched = true;
        for (int i = 0; i < mBottomNewsCount.size(); i++) {
            Integer cnt = mBottomNewsCount.get(mBottomNewsCount.keyAt(i));
            if (cnt != 0) {
                allFetched = false;
            }
        }
        if (allFetched) {
            NLLog.now("Bottom NewsContent fetch done.");
            mBottomAllFetched = true;
            saveDebug();
        }
    }

    private void saveDebug() {
        if (mTopFetched && (mBottomAllFetched || mBottomNewsCount.size() == 0)) {
            try {
                NewsDb.copyDbToExternalStorage(mActivity);
                NLLog.now("db copied");
            } catch (ExternalStorage.ExternalStorageException ignored) {
                // 디버그 모드에서만 작동해야 하므로 예외상황시 무시한다
            }
        }
    }

    public void onFetchAllNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        mTopNewsCount = topNewsFeed.getNewsList().size();
        mBottomNewsCount = new SparseArray<>();
        for (int i = 0 ; i < bottomNewsFeeds.size(); i++) {
            mBottomNewsCount.put(i, bottomNewsFeeds.get(i).getNewsList().size());
        }
    }
}
