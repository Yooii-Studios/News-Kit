package com.yooiistudios.newsflow.core.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Wooseong Kim in News L from Yooii Studios Co., LTD. on 15. 1. 21.
 *
 * TypefaceUtils
 *  폰트들을 싱글톤으로 관리
 */
public class TypefaceUtils {
    Typeface mNotoSansMediumTypeface;
    Typeface mNotoSansRegularTypeface;

    /**
     * SingletonClass
     */
    private volatile static TypefaceUtils instance;
    private TypefaceUtils() {}
    private static TypefaceUtils getInstance() {
        if (instance == null) {
            synchronized (TypefaceUtils.class) {
                if (instance == null) {
                    instance = new TypefaceUtils();
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
