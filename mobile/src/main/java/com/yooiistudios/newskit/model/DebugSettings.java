package com.yooiistudios.newskit.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * DebugSettings
 *  개발자들이 설정할 수 있는 옵션들을 제공하는 클래스
 */
public class DebugSettings {
    private static final String DEBUG_SETTINGS_SHARED_PREFERENCES = "DEBUG_SETTINGS_SHARED_PREFERENCES";
    private static final String START_DELAY_KEY = "START_DELAY_KEY";
    private static final String MID_DELAY_KEY = "MID_DELAY_KEY";
    private static final String DURATION_FOR_EACH_ITEM_KEY = "DURATION_FOR_EACH_ITEM_KEY";

    private static final int START_DELAY = 1000;
    private static final int MIDDLE_DELAY = 10;
    private static final int DURATION_FOR_EACH_ITEM = 400;

    private volatile static DebugSettings instance;
    private SharedPreferences prefs;
    private int startDelay;
    private int midDelay;
    private int durationForEachItem;

    private DebugSettings() {}
    private DebugSettings(Context context) {

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
                    instance.prefs = context.getSharedPreferences(DEBUG_SETTINGS_SHARED_PREFERENCES,
                            Context.MODE_PRIVATE);
                }
            }
        }
        return instance;
    }

    public static int getFirstDelay(Context context) {
        return getInstance(context).startDelay;
    }

    public static void setFistDelay(Context context, int startDelay) {
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
