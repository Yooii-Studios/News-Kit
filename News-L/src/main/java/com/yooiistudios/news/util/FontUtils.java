package com.yooiistudios.news.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Wooseong Kim in News L from Yooii Studios Co., LTD. on 15. 1. 21.
 *
 * FontUtils
 *  폰트들을 싱글톤으로 관리
 */
public class FontUtils {
    Typeface mNotoSansMediumTypeface;
    Typeface mNotoSansRegularTypeface;

    /**
     * SingletonClass
     */
    private volatile static FontUtils instance;
    private FontUtils() {}
    private static FontUtils getInstance() {
        if (instance == null) {
            synchronized (FontUtils.class) {
                if (instance == null) {
                    instance = new FontUtils();
                }
            }
        }
        return instance;
    }

    public static Typeface getMediumTypeface(Context context) {
        if (getInstance().mNotoSansMediumTypeface == null) {
            instance.mNotoSansMediumTypeface = Typeface.createFromAsset(context.getAssets(),
                    "NotoSansCJK-Medium.ttc");
        }
        return instance.mNotoSansMediumTypeface;
    }

    public static Typeface getRegularTypeface(Context context) {
        if (getInstance().mNotoSansRegularTypeface == null) {
            instance.mNotoSansRegularTypeface = Typeface.createFromAsset(context.getAssets(),
                    "NotoSansCJK-Regular.ttc");
        }
        return instance.mNotoSansRegularTypeface;
    }
}
