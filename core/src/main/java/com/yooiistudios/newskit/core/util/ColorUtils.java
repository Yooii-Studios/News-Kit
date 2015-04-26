package com.yooiistudios.newskit.core.util;

import android.graphics.Color;

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
        return Color.argb(alpha_int, Color.red(color),
                Color.green(color), Color.blue(color));
    }

    public static int scaleColor(int color, float factor, boolean scaleAlpha) {
        int alpha = scaleAlpha
                ? (Math.round(android.graphics.Color.alpha(color) * factor))
                : Color.alpha(color);

        return Color.argb(alpha,
                Math.round(Color.red(color) * factor),
                Math.round(Color.green(color) * factor),
                Math.round(Color.blue(color) * factor));
    }
}
