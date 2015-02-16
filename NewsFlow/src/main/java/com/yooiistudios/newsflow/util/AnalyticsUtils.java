package com.yooiistudios.newsflow.util;

import android.content.res.Configuration;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.iab.IabProducts;

import java.util.List;

/**
 * Created by Wooseong Kim in News from Yooii Studios Co., LTD. on 14. 12. 2.
 *
 * AnalyticsUtils
 *  Google Analytics 를 사용하기 위한 클래스
 */
public class AnalyticsUtils {
    private AnalyticsUtils() { throw new AssertionError("You must not create this class!"); }

    public static void startAnalytics(NewsApplication application, String screenName) {
        // Get tracker.
        Tracker t = application.getTracker(NewsApplication.TrackerName.APP_TRACKER);

        // it turn on the auto-tracking for post API-V14 devices
        // but you have to write a code on onStart and onStop for pre-V14 devices
        t.enableAutoActivityTracking(true);

        // Display Advertising: 타겟의 정보를 수집해서 구글 애널리틱으로 확인할 수 있는 옵션
        t.enableAdvertisingIdCollection(true);

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    // 메인화면 회전 체크
    public static void trackMainOrientation(NewsApplication application, String TAG, int orientation) {
        // Get tracker.
        Tracker t = application.getTracker(NewsApplication.TrackerName.APP_TRACKER);

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("Orientation")
                .setLabel(orientation == Configuration.ORIENTATION_PORTRAIT ? "Portrait" : "Landscape")
                .build());
    }

    // 풀버전 광고가 불려질 때 풀버전인지 아닌지 체크
    public static void trackInterstitialAd(NewsApplication application, String TAG) {
        List<String> ownedSkus = IabProducts.loadOwnedIabProducts(application);

        // Get tracker.
        Tracker t = application.getTracker(NewsApplication.TrackerName.APP_TRACKER);

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("Showing Ad when have PRO version")
                .setLabel(ownedSkus.contains(IabProducts.SKU_FULL_VERSION) ? "YES" : "NO")
                .build());
    }

    // 뉴스피드 디테일에서 뒤로 나가는 것을 up 버튼으로 하는지 back 버튼으로 하는지 조사하기 위해서 도입
    public static void trackNewsFeedDetailQuitAction(NewsApplication application, String TAG, String quitViewName) {
        // Get tracker.
        Tracker t = application.getTracker(NewsApplication.TrackerName.APP_TRACKER);

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("Quit NewsFeedDetailActivity with")
                .setLabel(quitViewName)
                .build());
    }

    // 세팅화면에서 뒤로 나가는 것을 up 버튼으로 하는지 back 버튼으로 하는지 조사하기 위해서 도입
    public static void trackSettingsQuitAction(NewsApplication application, String TAG, String quitViewName) {
        // Get tracker.
        Tracker t = application.getTracker(NewsApplication.TrackerName.APP_TRACKER);

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("Quit SettingActivity with")
                .setLabel(quitViewName)
                .build());
    }

    // 세팅 - 패널 매트릭스 선택을 트래킹
    public static void trackNewsPanelMatrixSelection(NewsApplication application, String TAG, String panelMatrix) {
        // Get tracker.
        Tracker t = application.getTracker(NewsApplication.TrackerName.APP_TRACKER);

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(TAG)
                .setAction("Newsfeed Panel Matrix Selection")
                .setLabel(panelMatrix)
                .build());
    }
}
