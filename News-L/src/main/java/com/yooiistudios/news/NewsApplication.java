package com.yooiistudios.news;

import android.app.Application;
import android.content.res.Configuration;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageType;

import java.util.Locale;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 24.
 *
 * NewsApplication
 */
public class NewsApplication extends Application {
    private RequestQueue mRequestQueue;
    private Locale mLocale = null;

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
        // Activity나 Application 등 UI스레드 아래와 같이 AsyncTask를 한번 호출합니다.
        // 메인스레드에서 단순히 클래스 로딩을 한번만 해도 AsyncTask내의 static 멤버 변수가 정상적으로 초기화됩니다.
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }
}
