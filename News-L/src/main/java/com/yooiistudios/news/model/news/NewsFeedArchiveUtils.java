package com.yooiistudios.news.model.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedArchiveUtil
 *  뉴스 피드 아카이빙 유틸
 */
public class NewsFeedArchiveUtils {
    private static final String SP_KEY_NEWS_FEED = "SP_KEY_NEWS_FEED";

    private static final String KEY_TOP_NEWS_FEED_URL = "KEY_TOP_NEWS_FEED_URL";
    private static final String KEY_TOP_NEWS_FEED = "KEY_TOP_NEWS_FEED";
    private static final String KEY_NEWS_FEED_RECENT_REFRESH = "KEY_NEWS_FEED_RECENT_REFRESH";

    private static final String KEY_BOTTOM_NEWS_FEED_URL_LIST = "KEY_BOTTOM_NEWS_FEED_URL_LIST";
    private static final String KEY_BOTTOM_NEWS_FEED_LIST = "KEY_BOTTOM_NEWS_FEED_LIST";

    // 60 Min * 60 Sec * 1000 millisec = 1 Hour
//    private static final long REFRESH_TERM_MILLISEC = 60 * 60 * 1000;
    private static final long REFRESH_TERM_MILLISEC = 10 * 1000;
//    private static final long REFRESH_TERM_MILLISEC = 24 * 60 * 60 * 1000;
    private static final long INVALID_REFRESH_TERM = -1;

    private NewsFeedArchiveUtils() {
        throw new AssertionError("You MUST not create this class!");
    }
    public static Pair<NewsFeedUrl, NewsFeed> loadTopNews(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);

        String newsFeedUrlStr = prefs.getString(KEY_TOP_NEWS_FEED_URL, null);
        String newsFeedStr = prefs.getString(KEY_TOP_NEWS_FEED, null);

        return makeUrlToFeedPair(newsFeedUrlStr, newsFeedStr);
    }
    public static Pair<ArrayList<NewsFeedUrl>, ArrayList<NewsFeed>> loadBottomNews(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);

        String newsFeedUrlListStr = prefs.getString(
                KEY_BOTTOM_NEWS_FEED_URL_LIST, null);
        String newsFeedListStr = prefs.getString(KEY_BOTTOM_NEWS_FEED_LIST, null);

        Type urlListType = new TypeToken<ArrayList<NewsFeedUrl>>(){}.getType();
        ArrayList<NewsFeedUrl> urlList;
        if (newsFeedUrlListStr != null) {
            urlList = new Gson().fromJson(newsFeedUrlListStr, urlListType);
        } else {
            urlList = NewsFeedPreset.sBottomNewsFeedPresetList;
        }

        Type feedListType = new TypeToken<ArrayList<NewsFeed>>(){}.getType();
        ArrayList<NewsFeed> feedList;
        if (newsFeedListStr != null) {
            feedList = new Gson().fromJson(newsFeedListStr, feedListType);
        } else {
            feedList = null;
        }

        return new Pair<ArrayList<NewsFeedUrl>, ArrayList<NewsFeed>>(urlList, feedList);
    }
    private static Pair<NewsFeedUrl, NewsFeed> makeUrlToFeedPair(
            String newsFeedUrlStr, String newsFeedStr) {
        Type urlType = new TypeToken<NewsFeedUrl>(){}.getType();
        NewsFeedUrl url;
        if (newsFeedUrlStr != null) {
            url = new Gson().fromJson(newsFeedUrlStr, urlType);
        } else {
            url = NewsFeedPreset.sTopNewsFeesPreset;
        }

        Type feedType = new TypeToken<NewsFeed>(){}.getType();
        NewsFeed newsFeed;
        if (newsFeedStr != null) {
            newsFeed = new Gson().fromJson(newsFeedStr, feedType);
        } else {
            newsFeed = null;
        }

        return new Pair<NewsFeedUrl, NewsFeed>(url, newsFeed);
    }
    public static void save(Context context,
                            @NonNull NewsFeedUrl topNewsFeedUrl, NewsFeed topNewsFeed,
                            @NonNull ArrayList<NewsFeedUrl> bottomNewsFeedUrlList,
                            ArrayList<NewsFeed> bottomNewsFeedList) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        putTopNewsFeed(editor, topNewsFeedUrl, topNewsFeed);
        putBottomNewsFeedList(editor, bottomNewsFeedUrlList, bottomNewsFeedList);

        // save recent saved millisec
        editor.putLong(KEY_NEWS_FEED_RECENT_REFRESH, System.currentTimeMillis());
        editor.apply();
    }
    private static void putTopNewsFeed(SharedPreferences.Editor editor,
                                       @NonNull NewsFeedUrl topNewsFeedUrl,
                                       NewsFeed topNewsFeed) {
        String topNewsFeedUrlStr = new Gson().toJson(topNewsFeedUrl);
        String topNewsFeedStr = topNewsFeed != null ? new Gson().toJson(topNewsFeed) : null;

        // save top news feed
        editor.putString(KEY_TOP_NEWS_FEED_URL, topNewsFeedUrlStr);
        if (topNewsFeedStr != null) {
            editor.putString(KEY_TOP_NEWS_FEED, topNewsFeedStr);
        } else {
            editor.remove(KEY_TOP_NEWS_FEED);
        }
    }

    private static void putBottomNewsFeedList(SharedPreferences.Editor editor,
                                              @NonNull ArrayList<NewsFeedUrl> bottomNewsFeedUrlList,
                                              ArrayList<NewsFeed> bottomNewsFeedList) {
        if (bottomNewsFeedList != null &&
                bottomNewsFeedUrlList.size() < bottomNewsFeedList.size()) {
            throw new IllegalArgumentException("bottomNewsFeedUrlList " +
                    "MUST contain more elements then bottomNewsFeedList.");
        }
        String bottomNewsFeedUrlListStr = new Gson().toJson(bottomNewsFeedUrlList);
        String bottomNewsFeedStr = new Gson().toJson(bottomNewsFeedList);

        // save bottom news feed
        editor.putString(KEY_BOTTOM_NEWS_FEED_URL_LIST, bottomNewsFeedUrlListStr);
        editor.putString(KEY_BOTTOM_NEWS_FEED_LIST, bottomNewsFeedStr);
    }
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SP_KEY_NEWS_FEED, Context.MODE_PRIVATE);
    }

    public static boolean newsNeedsToBeRefreshed(Context context) {
        long currentMillisec = System.currentTimeMillis();

        SharedPreferences prefs = getSharedPreferences(context);
        long recentRefreshMillisec = prefs.getLong(KEY_NEWS_FEED_RECENT_REFRESH, INVALID_REFRESH_TERM);

        if (recentRefreshMillisec == INVALID_REFRESH_TERM) {
            return true;
        }

        long gap = currentMillisec - recentRefreshMillisec;

        if (gap > REFRESH_TERM_MILLISEC) {
            return true;
        } else {
            return false;
        }
    }

    public static void clearArchive(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }
//    private static String getBottomUrlKey(int index) {
//        return KEY_BOTTOM_NEWS_FEED_URL_LIST + "_" + index;
//    }
//    private static String getBottomFeedKey(int index) {
//        return KEY_BOTTOM_NEWS_FEED_LIST + "_" + index;
//    }
//
//    public static void printSharedPreferences(Context context) {
//
//    }
}
