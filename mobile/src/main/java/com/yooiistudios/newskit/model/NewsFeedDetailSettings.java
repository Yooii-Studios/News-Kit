package com.yooiistudios.newskit.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * DebugSettings
 *  개발자들이 설정할 수 있는 옵션들을 제공하는 클래스
 */
public class NewsFeedDetailSettings {
    private static final String NEWSFEED_DETAIL_SETTINGS_PREFS = "newsfeed_detail_settings_prefs";
    private static final String AUTO_SCROLL_KEY = "auto_scroll_key";
    private static final String START_DELAY_KEY = "start_delay_key";
    private static final String MID_DELAY_KEY = "MID_DELAY_KEY";
    private static final String DURATION_FOR_EACH_ITEM_KEY = "duration_for_each_item_key";
    private static final String SPEED_KEY = "speed_item_key"; // 0 ~ 100(SeekBar)

    private static final int DEFAULT_START_DELAY = 3000;
    private static final int DEFAULT_MIDDLE_DELAY = 1500;
    private static final int DEFAULT_DURATION_FOR_EACH_ITEM = 3000;
    private static final int DEFAULT_SPEED = 50;

    private volatile static NewsFeedDetailSettings instance;
    private SharedPreferences prefs;
    private boolean isAutoScroll;
    private int startDelay;
    private int midDelay;
    private int durationForEachItem;
    private int speed;

    private NewsFeedDetailSettings() {}
    private NewsFeedDetailSettings(Context context) {
        prefs = context.getSharedPreferences(NEWSFEED_DETAIL_SETTINGS_PREFS, Context.MODE_PRIVATE);

        // 최초 설치시 디바이스의 언어와 비교해 앱이 지원하는 언어면 해당 언어로 설정, 아닐 경우 영어로 첫 언어 설정
        isAutoScroll = prefs.getBoolean(AUTO_SCROLL_KEY, true);
        startDelay = prefs.getInt(START_DELAY_KEY, DEFAULT_START_DELAY);
        midDelay = prefs.getInt(MID_DELAY_KEY, DEFAULT_MIDDLE_DELAY);
        durationForEachItem = prefs.getInt(DURATION_FOR_EACH_ITEM_KEY, DEFAULT_DURATION_FOR_EACH_ITEM);
        speed = prefs.getInt(SPEED_KEY, DEFAULT_SPEED);
    }

    public static NewsFeedDetailSettings getInstance(Context context) {
        if (instance == null) {
            synchronized (NewsFeedDetailSettings.class) {
                if (instance == null) {
                    instance = new NewsFeedDetailSettings(context);
                    instance.prefs = context.getSharedPreferences(NEWSFEED_DETAIL_SETTINGS_PREFS,
                            Context.MODE_PRIVATE);
                }
            }
        }
        return instance;
    }

    public static void setNewsFeedAutoScroll(Context context, boolean isAutoScroll) {
        getInstance(context).isAutoScroll = isAutoScroll;
        context.getSharedPreferences(NEWSFEED_DETAIL_SETTINGS_PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(AUTO_SCROLL_KEY, isAutoScroll).apply();
    }

    public static boolean isNewsFeedAutoScroll(Context context) {
        return getInstance(context).isAutoScroll;
    }

    // 초를 millisec 로 변환
    public static int getStartDelaySecond(Context context) {
        return getInstance(context).startDelay;
    }

    public static void setStartDelaySecond(Context context, int startDelaySecond) {
        getInstance(context).startDelay = startDelaySecond;
        getInstance(context).prefs.edit().putInt(START_DELAY_KEY, startDelaySecond).apply();
    }

    public static int getMidDelay(Context context) {
        return getInstance(context).midDelay;
    }

//    public static void setMidDelay(Context context, int midDelay) {
//        getInstance(context).midDelay = midDelay;
//        getInstance(context).prefs.edit().putInt(START_DELAY_KEY, midDelay).apply();
//    }

    public static int getDurationForEachItem(Context context) {
        return getInstance(context).durationForEachItem;
    }

    /*
    public static void setDurationForEachItem(Context context, int durationForEachItem) {
        getInstance(context).durationForEachItem = durationForEachItem;
        getInstance(context).prefs.edit().putInt(DURATION_FOR_EACH_ITEM_KEY, durationForEachItem).apply();
    }
    */

    public static int getSpeed(Context context) {
        return getInstance(context).speed;
    }

    public static void setSpeed(Context context, int speed) {
        getInstance(context).speed = speed;
        getInstance(context).prefs.edit().putInt(SPEED_KEY, speed).apply();
    }

    public static float getSpeedRatio(Context context) {
        // 스피드를 비율로 환산 = 100은 1/4배(빠르게), 0은 3배(느리게)
        // 식을 세워 보니 y = -11/400x + 3 이다.
        int speed = getSpeed(context);
        return (-11.f / 400.f * speed + 3.f);
    }
}
