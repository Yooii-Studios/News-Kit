package com.yooiistudios.news.model;

import android.content.Context;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 5.
 *
 * Settings
 *  세팅 탭의 여러 설정들을 관리
 *
 */
public class Settings {
    private static final String SETTINGS_SHARED_PREFERENCES = "settings_shared_preferences";
    private static final String NEWS_FEED_AUTO_SCROLL_KEY = "news_feed_auto_scroll_key";
    private static final String AUTO_REFRESH_INTERVAL_KEY = "auto_refresh_interval_key";
    private static final String AUTO_REFRESH_SPEED_KEY = "auto_refresh_speed_key";

    private Settings() { throw new AssertionError("You can't create this class!"); }

    public static void setNewsFeedAutoScroll(Context context, boolean isAutoScroll) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean(NEWS_FEED_AUTO_SCROLL_KEY, isAutoScroll).apply();
    }

    public static boolean isNewsFeedAutoScroll(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(NEWS_FEED_AUTO_SCROLL_KEY, true);
    }

    public static void setAutoRefreshInterval(Context context, int interval) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(AUTO_REFRESH_INTERVAL_KEY, interval).apply();
    }

    // available interval value is over 0
    public static int getAutoRefreshInterval(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(AUTO_REFRESH_INTERVAL_KEY, 7);
    }

    public static void setAutoRefreshSpeed(Context context, int speed) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(AUTO_REFRESH_SPEED_KEY, speed).apply();
    }

    // available speed value is between 0 and 100
    public static int getAutoRefreshSpeed(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(AUTO_REFRESH_SPEED_KEY, 50);
    }
}
