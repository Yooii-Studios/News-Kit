package com.yooiistudios.news.setting.language;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.yooiistudios.news.common.log.NLFlurry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 15.
 *
 * MNLanguage
 *  언어 설정을 관리
 */
public class NLLanguage {
    private static final String LANGUAGE_SHARED_PREFERENCES = "LANGUAGE_SHARED_PREFERENCES";
    private static final String LANGUAGE_MATRIX_KEY= "LANGUAGE_MATRIX_KEY";

    private volatile static NLLanguage instance;
    private NLLanguageType currentLanguageType;

    /**
     * Singleton
     */
    private NLLanguage(){}
    private NLLanguage(Context context) {
        int uniqueId = context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LANGUAGE_MATRIX_KEY, -1);

        // 최초 설치시 디바이스의 언어와 비교해 앱이 지원하는 언어면 해당 언어로 설정, 아닐 경우 영어로 첫 언어 설정
        if (uniqueId == -1) {
            Locale locale = Locale.getDefault();
            currentLanguageType = NLLanguageType.valueOfCodeAndRegion(locale.getLanguage(), locale.getCountry());
            // 아카이브
            context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(LANGUAGE_MATRIX_KEY, currentLanguageType.getUniqueId()).commit();
        } else {
            currentLanguageType = NLLanguageType.valueOfUniqueId(uniqueId);
        }
    }

    public static NLLanguage getInstance(Context context) {
        if (instance == null) {
            synchronized (NLLanguage.class) {
                if (instance == null) {
                    instance = new NLLanguage(context);
                }
            }
        }
        return instance;
    }

    public static NLLanguageType getCurrentLanguageType(Context context) { return NLLanguage.getInstance(context).currentLanguageType; }

    public static void setLanguageType(NLLanguageType newNewLanguage, Context context) {
        NLLanguage.getInstance(context).currentLanguageType = newNewLanguage;
        context.getSharedPreferences(LANGUAGE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(LANGUAGE_MATRIX_KEY, newNewLanguage.getUniqueId()).commit();

        // 플러리
        Map<String, String> params = new HashMap<String, String>();
        params.put(NLFlurry.LANGUAGE, newNewLanguage.toString());
        FlurryAgent.logEvent(NLFlurry.ON_SETTING_THEME, params);
    }
}
