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
    private static final String SETTINGS_SHARED_PREFERENCES = "SETTINGS_SHARED_PREFERENCES";
    private static final String NEWS_FEED_AUTO_SCROLL_KEY = "NEWS_FEED_AUTO_SCROLL_KEY";

    private Settings() { throw new AssertionError("You can't create this class!"); }

    public static void setNewsFeedAutoScroll(Context context, boolean isAutoScroll) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean(NEWS_FEED_AUTO_SCROLL_KEY, isAutoScroll).apply();
    }

    public static boolean isNewsFeedAutoScroll(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(NEWS_FEED_AUTO_SCROLL_KEY, true);
    }
}
