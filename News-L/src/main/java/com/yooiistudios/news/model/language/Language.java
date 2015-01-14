package com.yooiistudios.news.model.language;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import com.flurry.android.FlurryAgent;
import com.yooiistudios.news.util.FlurryUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 15.
 *
 * Language
 *  언어 설정을 관리
 */
public class Language {
    private static final String LANGUAGE_SHARED_PREFERENCES = "LANGUAGE_SHARED_PREFERENCES";
    private static final String LANGUAGE_MATRIX_KEY= "LANGUAGE_MATRIX_KEY";

    private volatile static Language instance;
    private LanguageType currentLanguageType;

    /**
     * Singleton
     */
    @SuppressWarnings("UnusedDeclaration")
    private Language() {}
    private Language(Context context) {
        int uniqueId = context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LANGUAGE_MATRIX_KEY, -1);

        // 최초 설치시 디바이스의 언어와 비교해 앱이 지원하는 언어면 해당 언어로 설정, 아닐 경우 영어로 첫 언어 설정
        if (uniqueId == -1) {
            Locale locale = Locale.getDefault();
            currentLanguageType = LanguageType.valueOfCodeAndRegion(locale.getLanguage(), locale.getCountry());
            // 아카이브
            context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(LANGUAGE_MATRIX_KEY, currentLanguageType.getUniqueId()).commit();
        } else {
            currentLanguageType = LanguageType.valueOfUniqueId(uniqueId);
        }
    }

    public static Language getInstance(Context context) {
        if (instance == null) {
            synchronized (Language.class) {
                if (instance == null) {
                    instance = new Language(context);
                }
            }
        }
        return instance;
    }

    public static LanguageType getCurrentLanguageType(Context context) { return Language.getInstance(context).currentLanguageType; }

    public static void setLanguageType(LanguageType newLanguage, Activity activity) {
        Context context = activity.getApplicationContext();

        // archive selection
        Language.getInstance(context).currentLanguageType = newLanguage;
        context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(LANGUAGE_MATRIX_KEY, newLanguage.getUniqueId()).commit();

        // update locale
        LanguageType currentLanguageType = Language.getCurrentLanguageType(activity);
        Locale locale = new Locale(currentLanguageType.getCode(), currentLanguageType.getRegion());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());

        // 플러리
        Map<String, String> params = new HashMap<>();
        params.put(FlurryUtils.LANGUAGE, newLanguage.toString());
        FlurryAgent.logEvent(FlurryUtils.ON_SETTING_THEME, params);
    }
}
