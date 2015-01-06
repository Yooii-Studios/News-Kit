package com.yooiistudios.news.util;

import android.content.res.Configuration;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.iab.IabProducts;

import java.util.List;

/**
 * Created by Wooseong Kim in MorningKit from Yooii Studios Co., LTD. on 14. 12. 2.
 *
 * MNAnalyticsUtils
 *  Google Analytics 를 사용하기 위한 클래스
 */
public class MNAnalyticsUtils {
    private MNAnalyticsUtils() { throw new AssertionError("You must not create this class!"); }

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
}
