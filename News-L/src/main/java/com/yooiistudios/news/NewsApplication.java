package com.yooiistudios.news;

import android.app.Application;
import android.content.res.Configuration;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageType;
import com.yooiistudios.news.util.InterpolatorHelper;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 24.
 *
 * NewsApplication
 */
public class NewsApplication extends Application {
    private Locale mLocale = null;
    private static final String PROPERTY_ID = "UA-50855812-11";

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mLocale != null) {
            newConfig.locale = mLocale;
            Locale.setDefault(mLocale);
            getApplicationContext().getResources().updateConfiguration(newConfig,
                    getApplicationContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        InterpolatorHelper.saveDefaultSetting(getApplicationContext());

        Configuration config = getApplicationContext().getResources().getConfiguration();

        // load language from MNLanguage
        LanguageType currentLanguageType = Language.getCurrentLanguageType(getApplicationContext());

        // update locale to current language
        mLocale = new Locale(currentLanguageType.getCode(), currentLanguageType.getRegion());
        Locale.setDefault(mLocale);
        config.locale = mLocale;
        getApplicationContext().getResources().updateConfiguration(config,
                getApplicationContext().getResources().getDisplayMetrics());

        // https://gist.github.com/benelog/5954649
        // AsyncTask가 UI 스레드가 아닌 곳에서 처음으로 호출된다면 에러스택이 발생할 수 있습니다.
        // 이 때 메인스레드에서 단순히 클래스 로딩을 한번만 해도 AsyncTask내의 static 멤버 변수가 정상적으로 초기화됩니다.
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
