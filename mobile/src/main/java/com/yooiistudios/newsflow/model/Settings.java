package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 5.
 *
 * Settings
 *  세팅 탭의 여러 설정들을 관리
 *
 */
public class Settings {
    private static final String SETTINGS_SHARED_PREFERENCES = "settings_shared_preferences";
    private static final String IS_FIRST_AUTO_REFRESH = "is_first_auto_refresh";
    private static final String NEWS_FEED_AUTO_SCROLL_KEY = "news_feed_auto_scroll_key";
    private static final String AUTO_REFRESH_INTERVAL_KEY = "auto_refresh_interval_key";
    private static final float AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS = 7;
    private static final String AUTO_REFRESH_SPEED_KEY = "auto_refresh_speed_key";
    private static final String IS_NOTIFICATION_ON_KEY = "is_notification_on_key";
    private static final String KEEP_SCREEN_ON_KEY = "keep_screen_on_key";

    private static final int AUTO_REFRESH_HANDLER_FIRST_DELAY = 4 * 1000;

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
                .getInt(AUTO_REFRESH_INTERVAL_KEY, (Math.round(AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS / 60 * 100)));
    }

    public static int getAutoRefreshInterval(Context context) {
        // available speed value is between 0 and 60(it converted from [0 ~ 100])
        float intervalProgress = getAutoRefreshIntervalProgress(context);
        return Math.round(intervalProgress / 100 * 60);
    }

    public static boolean isFirstAutoRefresh(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getBoolean(IS_FIRST_AUTO_REFRESH, true);
    }

    private static void setNotFirstAutoRefresh(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(IS_FIRST_AUTO_REFRESH, false).apply();
    }

    public static int getAutoRefreshHandlerDelay(Context context) {
        // 리프레시 간격과 속도와 패널 갯수를 구해서 다음 리프레시의 시간을 알아냄
        if (isFirstAutoRefresh(context)) {
            // 첫 리프레시 시에는 튜토리얼과 함께 짧은 간격 보여주기
            setNotFirstAutoRefresh(context);
            return AUTO_REFRESH_HANDLER_FIRST_DELAY;
        } else {
            // 전체 애니메이션 시간 = 뉴스 리프레시 간격 + 탑 스와이프 +
            // (바텀 각 애니메이션 * 갯수) - (바텀 각 애니메이션 딜레이 * (갯수 - 1))
            float autoRefreshSpeed = getAutoRefreshSpeed(context);

            int originalPanelAnimationHalfDuration = context.getResources().getInteger(
                    R.integer.bottom_news_feed_fade_anim_duration_milli);
            int panelAnimationDuration =
                    (int) (originalPanelAnimationHalfDuration * autoRefreshSpeed * 2);

            int originalPanelAnimationDelay =
                    context.getResources().getInteger(R.integer.bottom_news_feed_auto_refresh_delay_milli);
            int panelAnimationDelay = (int) (originalPanelAnimationDelay * autoRefreshSpeed);

            int autoRefreshIntervalMillis = getAutoRefreshInterval(context) * 1000;
            int panelCount = PanelMatrixUtils.getCurrentPanelMatrix(context).getPanelCount();

            return autoRefreshIntervalMillis + SlowSpeedScroller.SWIPE_DURATION +
                    panelAnimationDuration * panelCount - panelAnimationDelay * (panelCount - 1);
        }
    }

    public static void setAutoRefreshSpeedProgress(Context context, int speed) {
        // available speed value is between 0 and 100(SeekBar)
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(AUTO_REFRESH_SPEED_KEY, speed).apply();
    }

    public static int getAutoRefreshSpeedProgress(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(AUTO_REFRESH_SPEED_KEY, 50);
    }

    public static float getAutoRefreshSpeed(Context context) {
        // available speed value is between 1/2 of normal and 5 times of normal
        // 정상 속도의 1/2값 만큼 빠른 값부터 5배 값만큼 느린 값이 범위
        float speedProgress = getAutoRefreshSpeedProgress(context);
        if (speedProgress < 50) {
            // y = -4/50x + 5
            return -4.f / 50.f * speedProgress + 5;
        } else {
            // y = - 1/100x + 1.5
            return (float) (-1.f / 100.f * speedProgress + 1.5);
        }
    }

    public static void setNotification(Context context, boolean isNotificationOn) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean(IS_NOTIFICATION_ON_KEY, isNotificationOn).apply();
    }

    public static boolean isNotificationOn(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(IS_NOTIFICATION_ON_KEY, true);
    }

    public static void setKeepScreenOn(Context context, boolean isOn) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean(KEEP_SCREEN_ON_KEY, isOn).apply();
    }

    public static boolean isKeepScreenOn(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(KEEP_SCREEN_ON_KEY, true);
    }
}
