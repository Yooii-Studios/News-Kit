package com.yooiistudios.newsflow.core.news.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.yooiistudios.newsflow.core.news.database.NewsDb;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedArchiveUtil
 *  뉴스 피드 아카이빙 유틸
 */
public class NewsFeedArchiveUtils {
    private static final String SP_KEY_NEWS_FEED = "SP_KEY_NEWS_FEED";

    private static final String KEY_NEWS_FEED_RECENT_REFRESH = "KEY_NEWS_FEED_RECENT_REFRESH";
    // 백그라운드 캐시가 읽힌 적이 있는지(유저가 본 적이 있는지)에 대한 정보
    private static final String KEY_BACKGROUND_CACHE_AVAILABLE = "KEY_BACKGROUND_CACHE_AVAILABLE";
    // 유저가 처음 백그라운드 캐시를 읽은 시각
    private static final String KEY_BACKGROUND_CACHE_READ_TIME = "KEY_BACKGROUND_CACHE_READ_TIME";

    // 6 Hours * 60 Min * 60 Sec * 1000 millisec = 6 Hours
    private static final long CACHE_EXPIRATION_LIMIT = 6 * 60 * 60 * 1000;
//    private static final long CACHE_EXPIRATION_LIMIT = 10 * 1000;
//    private static final long CACHE_EXPIRATION_LIMIT = 24 * 60 * 60 * 1000;
    private static final long INVALID_REFRESH_TERM = -1;
    private static final long INVALID_CACHE_READ_TIME = -1;

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
        return newsNeedsToBeRefreshed(context, CACHE_EXPIRATION_LIMIT);
    }

    public static boolean newsNeedsToBeRefreshed(Context context, long expireLimit) {
        long currentMillisec = System.currentTimeMillis();
        long recentRefreshMillisec = getRecentRefreshMillisec(context);
        if (recentRefreshMillisec == INVALID_REFRESH_TERM) {
            return true;
        }
        long timePastSinceRecentRefresh = currentMillisec - recentRefreshMillisec;

        boolean isCacheExpired = timePastSinceRecentRefresh > expireLimit;

        long timePastSinceFirstCacheRead = currentMillisec - getBackgroundCacheReadTime(context);
        boolean backgroundCacheAvailable = isBackgroundCacheAvailable(context)
                || timePastSinceFirstCacheRead < 10 * 60 * 1000;

        return isCacheExpired || !backgroundCacheAvailable;
    }

    public static long getRecentRefreshMillisec(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getLong(KEY_NEWS_FEED_RECENT_REFRESH, INVALID_REFRESH_TERM);
    }

    public static void saveBackgroundCacheAvailable(Context context) {
        if (isBackgroundCacheUnavailable(context)) {
            getSharedPreferences(context).edit()
                    .putBoolean(KEY_BACKGROUND_CACHE_AVAILABLE, true)
                    .putLong(KEY_BACKGROUND_CACHE_READ_TIME, INVALID_CACHE_READ_TIME)
                    .apply();
        }
    }

    public static void saveBackgroundCacheUnavailable(Context context) {
        if (isBackgroundCacheAvailable(context)) {
            getSharedPreferences(context).edit()
                    .putBoolean(KEY_BACKGROUND_CACHE_AVAILABLE, false)
                    .putLong(KEY_BACKGROUND_CACHE_READ_TIME, System.currentTimeMillis())
                    .apply();
        }
    }

    public static boolean isBackgroundCacheAvailable(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_BACKGROUND_CACHE_AVAILABLE, false);
    }

    private static long getBackgroundCacheReadTime(Context context) {
        return getSharedPreferences(context)
                .getLong(KEY_BACKGROUND_CACHE_READ_TIME, INVALID_CACHE_READ_TIME);
    }

    public static boolean isBackgroundCacheUnavailable(Context context) {
        return !isBackgroundCacheAvailable(context);
    }

    public static void clearArchive(Context context) {
        NewsDb.getInstance(context).clearArchive();
        getSharedPreferences(context).edit().clear().apply();
    }
}
