package com.yooiistudios.newsflow.util;

import java.lang.Math;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 24.
 *
 * NLColor
 *  구글 I/O에서 가져온 컬러 관련 유틸리티 클래스
 */
public class ColorUtils {
    private ColorUtils() { throw new AssertionError("MUST not create this class!"); }

    public static int setColorAlpha(int color, float alpha) {
        int alpha_int = Math.min(Math.max((int)(alpha * 255.0f), 0), 255);
        return android.graphics.Color.argb(alpha_int, android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color));
    }

    public static int scaleColor(int color, float factor, boolean scaleAlpha) {
        return android.graphics.Color.argb(scaleAlpha ? (Math.round(android.graphics.Color.alpha(color) * factor)) : android.graphics.Color.alpha(color),
                Math.round(android.graphics.Color.red(color) * factor), Math.round(android.graphics.Color.green(color) * factor),
                Math.round(android.graphics.Color.blue(color) * factor));
    }

}
