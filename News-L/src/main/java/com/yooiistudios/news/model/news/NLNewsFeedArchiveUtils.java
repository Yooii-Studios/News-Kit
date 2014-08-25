package com.yooiistudios.news.model.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yooiistudios.news.model.NLNewsFeedUrl;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedArchiveUtil
 *  뉴스 피드 아카이빙 유틸
 */
public class NLNewsFeedArchiveUtils {
    private static final String SP_KEY_NEWS_FEED = "SP_KEY_NEWS_FEED";

    private static final String KEY_TOP_NEWS_FEED_URL = "KEY_TOP_NEWS_FEED_URL";
    private static final String KEY_TOP_NEWS_FEED = "KEY_TOP_NEWS_FEED";
    private static final String KEY_NEWS_FEED_RECENT_REFRESH = "KEY_NEWS_FEED_RECENT_REFRESH";

    private static final String KEY_BOTTOM_NEWS_FEED_URL_LIST = "KEY_BOTTOM_NEWS_FEED_URL_LIST";
    private static final String KEY_BOTTOM_NEWS_FEED_LIST = "KEY_BOTTOM_NEWS_FEED_LIST";

    // 60 Min * 60 Sec * 1000 millisec = 1 Hour
    private static final long REFRESH_TERM_MILLISEC = 60 * 60 * 1000;
    private static final long INVALID_REFRESH_TERM = -1;

    private NLNewsFeedArchiveUtils() {
        throw new AssertionError("You MUST not create this class!");
    }
    public static Pair<NLNewsFeedUrl, NLNewsFeed> loadTopNews(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);

        String newsFeedUrlStr = prefs.getString(KEY_TOP_NEWS_FEED_URL, null);
        String newsFeedStr = prefs.getString(KEY_TOP_NEWS_FEED, null);

        return makeUrlToFeedPair(newsFeedUrlStr, newsFeedStr);
    }
    public static Pair<ArrayList<NLNewsFeedUrl>, ArrayList<NLNewsFeed>> loadBottomNews(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);

        String newsFeedUrlListStr = prefs.getString(
                KEY_BOTTOM_NEWS_FEED_URL_LIST, null);
        String newsFeedListStr = prefs.getString(KEY_BOTTOM_NEWS_FEED_LIST, null);

        Type urlListType = new TypeToken<ArrayList<NLNewsFeedUrl>>(){}.getType();
        ArrayList<NLNewsFeedUrl> urlList;
        if (newsFeedUrlListStr != null) {
            urlList = new Gson().fromJson(newsFeedUrlListStr, urlListType);
        } else {
            urlList = NLNewsFeedPreset.sBottomNewsFeedPresetList;
        }

        Type feedListType = new TypeToken<ArrayList<NLNewsFeed>>(){}.getType();
        ArrayList<NLNewsFeed> feedList;
        if (newsFeedListStr != null) {
            feedList = new Gson().fromJson(newsFeedListStr, feedListType);
        } else {
            feedList = null;
        }

        return new Pair<ArrayList<NLNewsFeedUrl>, ArrayList<NLNewsFeed>>(urlList, feedList);
    }
    private static Pair<NLNewsFeedUrl, NLNewsFeed> makeUrlToFeedPair(
            String newsFeedUrlStr, String newsFeedStr) {
        Type urlType = new TypeToken<NLNewsFeedUrl>(){}.getType();
        NLNewsFeedUrl url;
        if (newsFeedUrlStr != null) {
            url = new Gson().fromJson(newsFeedUrlStr, urlType);
        } else {
            url = NLNewsFeedPreset.sTopNewsFeesPreset;
        }

        Type feedType = new TypeToken<NLNewsFeed>(){}.getType();
        NLNewsFeed newsFeed;
        if (newsFeedStr != null) {
            newsFeed = new Gson().fromJson(newsFeedStr, feedType);
        } else {
            newsFeed = null;
        }

        return new Pair<NLNewsFeedUrl, NLNewsFeed>(url, newsFeed);
    }
    public static void save(Context context,
                            @NonNull NLNewsFeedUrl topNewsFeedUrl, NLNewsFeed topNewsFeed,
                            @NonNull ArrayList<NLNewsFeedUrl> bottomNewsFeedUrlList,
                            ArrayList<NLNewsFeed> bottomNewsFeedList) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        putTopNewsFeed(editor, topNewsFeedUrl, topNewsFeed);
        putBottomNewsFeedList(editor, bottomNewsFeedUrlList, bottomNewsFeedList);

        // save recent saved millisec
        editor.putLong(KEY_NEWS_FEED_RECENT_REFRESH, System.currentTimeMillis());
        editor.apply();
    }
    private static void putTopNewsFeed(SharedPreferences.Editor editor,
                                       @NonNull NLNewsFeedUrl topNewsFeedUrl,
                                       NLNewsFeed topNewsFeed) {
        String topNewsFeedUrlStr = new Gson().toJson(topNewsFeedUrl);
        String topNewsFeedStr = new Gson().toJson(topNewsFeed);

        // save top news feed
        editor.putString(KEY_TOP_NEWS_FEED_URL, topNewsFeedUrlStr);
        editor.putString(KEY_TOP_NEWS_FEED, topNewsFeedStr);
    }

    private static void putBottomNewsFeedList(SharedPreferences.Editor editor,
                                              @NonNull ArrayList<NLNewsFeedUrl> bottomNewsFeedUrlList,
                                              ArrayList<NLNewsFeed> bottomNewsFeedList) {
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
