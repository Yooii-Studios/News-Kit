package com.yooiistudios.newskit;

import android.app.Application;
import android.content.res.Configuration;
import android.os.StrictMode;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.yooiistudios.newskit.core.debug.DebugSettings;
import com.yooiistudios.newskit.core.language.Language;
import com.yooiistudios.newskit.core.language.LanguageUtils;
import com.yooiistudios.newskit.util.InterpolatorHelper;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 24.
 *
 * NewsApplication
 */
public class NewsApplication extends Application {
    private Locale mLocale = null;
    private static final String PROPERTY_ID = "UA-50855812-24";

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

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

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
        enableStrictMode();
        super.onCreate();

        InterpolatorHelper.saveDefaultSetting(getApplicationContext());

        Configuration config = getApplicationContext().getResources().getConfiguration();

        // load language from MNLanguage
        Language currentLanguage = LanguageUtils.getCurrentLanguage(getApplicationContext());

        // update locale to current language
        mLocale = new Locale(currentLanguage.getLanguageCode(), currentLanguage.getRegion());
        Locale.setDefault(mLocale);
        config.locale = mLocale;
        getApplicationContext().getResources().updateConfiguration(config,
                getApplicationContext().getResources().getDisplayMetrics());

        // https://gist.github.com/benelog/5954649
        // AsyncTask 가 UI 스레드가 아닌 곳에서 처음으로 호출된다면 에러스택이 발생할 수 있습니다.
        // 이 때 메인스레드에서 단순히 클래스 로딩을 한번만 해도 AsyncTask 내의 static 멤버 변수가 정상적으로 초기화됩니다.
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 타입페이스 부르는 시간이 많이 걸리기 때문에 이를 미리 불러주게 변경
        /*
        new android.os.AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                TypefaceUtils.getMediumTypeface(getApplicationContext());
                TypefaceUtils.getRegularTypeface(getApplicationContext());
                return null;
            }
        }.execute();
        */
    }

    /**
     * 엄격 모드는 super.onCreate() 이전에 불러야 하는 듯.
     * @see android.os.StrictMode
     */
    private void enableStrictMode() {
        if (DebugSettings.debugStrictMode()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .penaltyFlashScreen() // 테스트용으로 넣어봄
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDropBox()
                    .build());
        }
    }

}
