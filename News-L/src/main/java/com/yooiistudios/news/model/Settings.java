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
    private static final float AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS = 7;
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

    public static void setAutoRefreshIntervalProgress(Context context, int interval) {
        // available speed value is between 0 and 100(SeekBar)
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(AUTO_REFRESH_INTERVAL_KEY, interval).apply();
    }

    public static int getAutoRefreshIntervalProgress(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(AUTO_REFRESH_INTERVAL_KEY, (int) (AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS / 60 * 100));
    }

    public static int getAutoRefreshInterval(Context context) {
        // available speed value is between 0 and 60(it converted from [0 ~ 100])
        float intervalProgress = getAutoRefreshIntervalProgress(context);
        return (int) (intervalProgress / 100 * 60);
    }

    public static void setAutoRefreshSpeedProgress(Context context, int speed) {
        // available speed value is between 0 and 100(SeekBar)
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(AUTO_REFRESH_SPEED_KEY, speed).apply();
    }

    // it will converted XX ~ XX seconds
    public static int getAutoRefreshSpeedProgress(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(AUTO_REFRESH_SPEED_KEY, 50);
    }

    public static float getAutoRefreshSpeed(Context context) {
        // available speed value is between 1/2 of normal and 5 times of normal
        // 1/2값 ~ 5배 값
        float speedProgress = getAutoRefreshSpeedProgress(context);
        if (speedProgress < 50) {
            // y = -4/50x + 5
            return -4.f / 50.f * speedProgress + 5;
        } else {
            // y = - 1/100x + 1.5
            return (float) (-1.f / 100.f * speedProgress + 1.5);
        }
    }
}
