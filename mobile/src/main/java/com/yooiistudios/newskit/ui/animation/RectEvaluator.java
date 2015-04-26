package com.yooiistudios.newskit.ui.animation;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 27.
 *
 * RectEvaluator
 *  Rect 객체를 사용하는 TypeEvaluator
 */

import android.animation.TypeEvaluator;
import android.graphics.Rect;

public class RectEvaluator implements TypeEvaluator<Rect> {

    private Rect mRect;

    public RectEvaluator() {
    }

    public RectEvaluator(Rect reuseRect) {
        mRect = reuseRect;
    }

    @Override
    public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
        int left = startValue.left + (int) ((endValue.left - startValue.left) * fraction);
        int top = startValue.top + (int) ((endValue.top - startValue.top) * fraction);
        int right = startValue.right + (int) ((endValue.right - startValue.right) * fraction);
        int bottom = startValue.bottom + (int) ((endValue.bottom - startValue.bottom) * fraction);
        if (mRect == null) {
            return new Rect(left, top, right, bottom);
        } else {
            mRect.set(left, top, right, bottom);
            return mRect;
        }
    }
}
