package com.yooiistudios.news.model.debug;

import android.content.Context;
import android.content.SharedPreferences;

import static com.yooiistudios.news.ui.activity.NewsFeedDetailActivity.START_DELAY;
import static com.yooiistudios.news.ui.activity.NewsFeedDetailActivity.MIDDLE_DELAY;
import static com.yooiistudios.news.ui.activity.NewsFeedDetailActivity.DURATION_FOR_EACH_ITEM;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * DebugSettings
 *  개발자들이 설정할 수 있는 옵션들을 제공하는 클래스
 */
public class DebugSettings {
    protected static final String DEBUG_SETTINGS_SHARED_PREFERENCES = "DEBUG_SETTINGS_SHARED_PREFERENCES";
    protected static final String START_DELAY_KEY = "START_DELAY_KEY";
    protected static final String MID_DELAY_KEY = "MID_DELAY_KEY";
    protected static final String DURATION_FOR_EACH_ITEM_KEY = "DURATION_FOR_EACH_ITEM_KEY";

    private volatile static DebugSettings instance;
    private SharedPreferences prefs;
    private int startDelay;
    private int midDelay;
    private int durationForEachItem;

    // Singleton
    private DebugSettings(Context context) {
        prefs = context.getSharedPreferences(DEBUG_SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // 최초 설치시 디바이스의 언어와 비교해 앱이 지원하는 언어면 해당 언어로 설정, 아닐 경우 영어로 첫 언어 설정
        startDelay = prefs.getInt(START_DELAY_KEY, START_DELAY);
        midDelay = prefs.getInt(MID_DELAY_KEY, MIDDLE_DELAY);
        durationForEachItem = prefs.getInt(DURATION_FOR_EACH_ITEM_KEY, DURATION_FOR_EACH_ITEM);
    }

    public static DebugSettings getInstance(Context context) {
        if (instance == null) {
            synchronized (DebugSettings.class) {
                if (instance == null) {
                    instance = new DebugSettings(context);
                }
            }
        }
        return instance;
    }

    // Getter / Setter
    public static int getStartDelay(Context context) {
        return getInstance(context).startDelay;
    }

    public static void setStartDelay(Context context, int startDelay) {
        getInstance(context).startDelay = startDelay;
        getInstance(context).prefs.edit().putInt(START_DELAY_KEY, startDelay).apply();
    }

    public static int getMidDelay(Context context) {
        return getInstance(context).midDelay;
    }

    public static void setMidDelay(Context context, int midDelay) {
        getInstance(context).midDelay = midDelay;
        getInstance(context).prefs.edit().putInt(START_DELAY_KEY, midDelay).apply();
    }

    public static int getDurationForEachItem(Context context) {
        return getInstance(context).durationForEachItem;
    }

    public static void setDurationForEachItem(Context context, int durationForEachItem) {
        getInstance(context).durationForEachItem = durationForEachItem;
        getInstance(context).prefs.edit().putInt(DURATION_FOR_EACH_ITEM_KEY, durationForEachItem).apply();
    }
}
