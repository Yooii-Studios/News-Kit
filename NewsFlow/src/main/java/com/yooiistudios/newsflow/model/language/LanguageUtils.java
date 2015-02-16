package com.yooiistudios.newsflow.model.language;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import com.flurry.android.FlurryAgent;
import com.yooiistudios.newsflow.util.FlurryUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 15.
 *
 * Language
 *  언어 설정을 관리
 */
public class LanguageUtils {
    private static final String LANGUAGE_SHARED_PREFERENCES = "LANGUAGE_SHARED_PREFERENCES";
    private static final String LANGUAGE_MATRIX_KEY= "LANGUAGE_MATRIX_KEY";

    private volatile static LanguageUtils instance;
    private Language mCurrentLanguage;

    /**
     * Singleton
     */
    @SuppressWarnings("UnusedDeclaration")
    private LanguageUtils() {}
    private LanguageUtils(Context context) {
        int uniqueId = context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LANGUAGE_MATRIX_KEY, -1);

        // 최초 설치시 디바이스의 언어와 비교해 앱이 지원하는 언어면 해당 언어로 설정, 아닐 경우 영어로 첫 언어 설정
        if (uniqueId == -1) {
            Locale locale = Locale.getDefault();
            mCurrentLanguage = Language.valueOfCodeAndRegion(locale.getLanguage(), locale.getCountry());
            // 아카이브
            context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(LANGUAGE_MATRIX_KEY, mCurrentLanguage.getUniqueId()).commit();
        } else {
            mCurrentLanguage = Language.valueOfUniqueId(uniqueId);
        }
    }

    public static LanguageUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (LanguageUtils.class) {
                if (instance == null) {
                    instance = new LanguageUtils(context);
                }
            }
        }
        return instance;
    }

    public static Language getCurrentLanguageType(Context context) { return LanguageUtils.getInstance(context).mCurrentLanguage; }

    public static void setLanguageType(Language newLanguage, Activity activity) {
        Context context = activity.getApplicationContext();

        // archive selection
        LanguageUtils.getInstance(context).mCurrentLanguage = newLanguage;
        context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(LANGUAGE_MATRIX_KEY, newLanguage.getUniqueId()).commit();

        // update locale
        Language currentLanguage = LanguageUtils.getCurrentLanguageType(activity);
        Locale locale = new Locale(currentLanguage.getCode(), currentLanguage.getRegion());
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
