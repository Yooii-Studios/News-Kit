package com.yooiistudios.newsflow.model.news.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedArchiveUtil
 *  뉴스 피드 아카이빙 유틸
 */
public class NewsFeedArchiveUtils {
    private static final String SP_KEY_NEWS_FEED = "SP_KEY_NEWS_FEED";

    private static final String KEY_NEWS_FEED_RECENT_REFRESH = "KEY_NEWS_FEED_RECENT_REFRESH";

    // 6 Hours * 60 Min * 60 Sec * 1000 millisec = 6 Hours
    private static final long CACHE_EXPIRATION_LIMIT = 6 * 60 * 60 * 1000;
//    private static final long CACHE_EXPIRATION_LIMIT = 10 * 1000;
//    private static final long CACHE_EXPIRATION_LIMIT = 24 * 60 * 60 * 1000;
    private static final long INVALID_REFRESH_TERM = -1;

    private NewsFeedArchiveUtils() {
        throw new AssertionError("You MUST not create this class!");
    }

    public static void saveRecentCacheMillisec(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(KEY_NEWS_FEED_RECENT_REFRESH, System.currentTimeMillis());

        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SP_KEY_NEWS_FEED, Context.MODE_PRIVATE);
    }

    public static boolean newsNeedsToBeRefreshed(Context context) {
        long currentMillisec = System.currentTimeMillis();

        long recentRefreshMillisec = getRecentRefreshMillisec(context);

        if (recentRefreshMillisec == INVALID_REFRESH_TERM) {
            return true;
        }

        long gap = currentMillisec - recentRefreshMillisec;

        return gap > CACHE_EXPIRATION_LIMIT;
    }

    public static long getRecentRefreshMillisec(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getLong(KEY_NEWS_FEED_RECENT_REFRESH, INVALID_REFRESH_TERM);
    }

    public static void clearArchive(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }
}
