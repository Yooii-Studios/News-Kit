package com.yooiistudios.newsflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.yooiistudios.newsflow.model.Settings;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 9.
 *
 * SettingsTest
 *  세팅 관련 사항들을 테스트
 */
public class SettingsTest extends AndroidTestCase {
    // private 을 깨기 싫어서 Settings 에서 가져옴. 후에 변경될 가능성이 있는 값들이라 기획 변경에 따라 수정해줄 것
    private static final String SETTINGS_SHARED_PREFERENCES = "settings_shared_preferences";
    private static final float AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS = 7;
    private static final int AUTO_REFRESH_HANDLER_FIRST_DELAY = 1000;

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDefaultAutoRefreshSpeed() {
        clearPrefs();

        // 기본 progress 값은 50, 배율은 정상 속도의 1배
        assertEquals(50, Settings.getAutoRefreshSpeedProgress(mContext));
        assertEquals(1.f, Settings.getAutoRefreshSpeed(mContext));
    }

    public void testAutoRefreshSpeed() {
        clearPrefs();

        Settings.setAutoRefreshSpeedProgress(mContext, 0);

        // 최저 속도는 정상 속도보다 5배 느림(5배)
        assertEquals(0, Settings.getAutoRefreshSpeedProgress(mContext));
        assertEquals(5.f, Settings.getAutoRefreshSpeed(mContext));

        Settings.setAutoRefreshSpeedProgress(mContext, 100);

        // 최고 속도는 정상 속도보다 2배 빠름(1/2배)
        assertEquals(100, Settings.getAutoRefreshSpeedProgress(mContext));
        assertEquals(0.5f, Settings.getAutoRefreshSpeed(mContext));
    }

    public void testDefaultAutoRefreshInterval() {
        clearPrefs();

        // 기본 progress 값은 기본 초(7초)를 progress 로 환산한 값
        assertEquals(Math.round(AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS * 100 / 60),
                 Settings.getAutoRefreshIntervalProgress(mContext));
        // progress 를 초로 환산하면 정확하게 기본 초(7초)가 되지는 않는다.. 따라서 Math.round 를 사용하게 변경
        assertEquals((int) AUTO_REFRESH_INTERVAL_DEFAULT_SECONDS, Settings.getAutoRefreshInterval(mContext));
    }

    public void testAutoRefreshInterval() {
        clearPrefs();

        Settings.setAutoRefreshIntervalProgress(mContext, 0);

        // 최저 시간은 0초
        assertEquals(0, Settings.getAutoRefreshIntervalProgress(mContext));
        assertEquals(0, Settings.getAutoRefreshInterval(mContext));

        Settings.setAutoRefreshIntervalProgress(mContext, 100);

        // 최고 시간은 60초
        assertEquals(100, Settings.getAutoRefreshIntervalProgress(mContext));
        assertEquals(60, Settings.getAutoRefreshInterval(mContext));
    }

    public void testAutoRefreshHanderDelay() {
        clearPrefs();

        // 첫 리프레시 시간은 정해진 값으로 = 4초, 두 번째 부터는 패널 갯수와 Interval 에 따라 계산
        int firstAutoRefreshHandlerDelay = Settings.getAutoRefreshHandlerDelay(mContext);
        int secondAutoRefreshHandlerDelay = Settings.getAutoRefreshHandlerDelay(mContext);

        assertEquals(AUTO_REFRESH_HANDLER_FIRST_DELAY, firstAutoRefreshHandlerDelay);
        assertNotSame(AUTO_REFRESH_HANDLER_FIRST_DELAY, secondAutoRefreshHandlerDelay);
    }

    private void clearPrefs() {
        SharedPreferences prefs = mContext.getSharedPreferences(SETTINGS_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
