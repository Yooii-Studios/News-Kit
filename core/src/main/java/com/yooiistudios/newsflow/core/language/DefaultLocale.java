package com.yooiistudios.newsflow.core.language;

import android.content.Context;
import android.content.SharedPreferences;

import com.yooiistudios.newsflow.core.util.NLLog;

import java.util.Locale;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 4. 13.
 *
 * DefaultLocale
 *  앱의 첫 실행 시 디바이스의 기존 Locale 을 저장. 이를 사용해 초기 뉴스를 세팅하는 데 사용할 것
 */
public class DefaultLocale {
    private static final String LOCALE_SHARED_PREFERENCES = "locale_shared_preferences";
    private static final String COUNTRY_KEY = "country_key";
    private static final String LANGUAGE_KEY = "language_key";

    public static void saveDefaultLocale(Context context, Locale locale) {
        SharedPreferences prefs =
                context.getSharedPreferences(LOCALE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        NLLog.now("COUNTRY_KEY: " + locale.getCountry());
        NLLog.now("LANGUAGE_KEY: " + locale.getLanguage());
        prefs.edit().putString(COUNTRY_KEY, locale.getCountry()).apply();
        prefs.edit().putString(LANGUAGE_KEY, locale.getLanguage()).apply();
    }

    public static Locale loadDefaultLocale(Context context) {
        // 제대로 읽히지 않을경우 미국-영어로 저장
        SharedPreferences prefs =
                context.getSharedPreferences(LOCALE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String country = prefs.getString(COUNTRY_KEY, "US");
        String language = prefs.getString(LANGUAGE_KEY, "en");
        return new Locale(language, country);
    }
}
