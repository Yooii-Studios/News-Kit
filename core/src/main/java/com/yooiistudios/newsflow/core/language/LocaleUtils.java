package com.yooiistudios.newsflow.core.language;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by Wooseong Kim in MorningKit from Yooii Studios Co., LTD. on 2014. 8. 22.
 *
 * MNLocaleUtils
 *  필요할 때 언어가 원래대로 돌아가는 문제를 해결하기 위해 로케일을 새로 적용해 주는 유틸 클래스
 */
public class LocaleUtils {
    private LocaleUtils() { throw new AssertionError("You MUST not create this class!"); }

    public static void updateLocale(Context context) {
        // 회전마다 Locale 을 새로 적용해줌(언어가 바뀌어 버리는 문제 해결)
        Configuration config = context.getApplicationContext().getResources().getConfiguration();
        Language currentLanguage = LanguageUtils.getCurrentLanguage(context);
        Locale locale = new Locale(currentLanguage.getLanguageCode(), currentLanguage.getRegion());
        Locale.setDefault(locale);
        config.locale = locale;
        context.getApplicationContext().getResources().updateConfiguration(config,
                context.getApplicationContext().getResources().getDisplayMetrics());
    }
}
