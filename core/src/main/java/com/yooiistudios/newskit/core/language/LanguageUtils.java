package com.yooiistudios.newskit.core.language;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 2014. 1. 15.
 *
 * Language
 *  언어 설정을 관리
 */
public class LanguageUtils {
    private static final String LANGUAGE_SHARED_PREFERENCES = "language_shared_preferences";
    private static final String LANGUAGE_KEY = "language_key";

    private volatile static LanguageUtils instance;
    private Language mCurrentLanguage;

    /**
     * Singleton
     */
    @SuppressWarnings("UnusedDeclaration")
    private LanguageUtils() {}
    private LanguageUtils(Context context) {
        int uniqueId = context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LANGUAGE_KEY, -1);

        // 최초 설치시 디바이스의 언어와 비교해 앱이 지원하는 언어면 해당 언어로 설정, 아닐 경우 영어로 첫 언어 설정
        if (uniqueId == -1) {
            Locale locale = Locale.getDefault();
            mCurrentLanguage = Language.valueOfCodeAndRegion(locale.getLanguage(), locale.getCountry());
            // 아카이브
            context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(LANGUAGE_KEY, mCurrentLanguage.getUniqueId()).apply();

            // 기존 Locale 을 저장. 초기 패널 지정시 사용
            DefaultLocale.saveDefaultLocale(context, locale);
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

    public static Language getCurrentLanguage(Context context) {
        return LanguageUtils.getInstance(context).mCurrentLanguage;
    }

    public static void setCurrentLanguage(Language newLanguage, Context context) {
        // archive selection
        LanguageUtils.getInstance(context).mCurrentLanguage = newLanguage;
        context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(LANGUAGE_KEY, newLanguage.getUniqueId()).apply();

        // update locale
        Language currentLanguage = LanguageUtils.getCurrentLanguage(context);
        Locale locale = new Locale(currentLanguage.getLanguageCode(), currentLanguage.getRegion());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        /*
        // 플러리
        Map<String, String> params = new HashMap<>();
        params.put(FlurryUtils.LANGUAGE, newLanguage.toString());
        FlurryAgent.logEvent(FlurryUtils.ON_SETTING_THEME, params);
        */
    }

    // 테스트를 위해 언어 설정을 초기화, 새로 앱을 실행할 때 기기 언어 나라별 뉴스 세팅을 테스트하기 위함
    public static void resetLanguageDebug(Context context) {
        context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().remove(LANGUAGE_KEY).apply();
    }
}
