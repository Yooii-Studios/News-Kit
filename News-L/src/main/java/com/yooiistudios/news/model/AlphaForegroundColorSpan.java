package com.yooiistudios.news.model;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 1.
 *
 * AlphaForegroundColorSpan
 *  텍스트의 알파값을 애니메이트 하기 위한 color span
 */
public class AlphaForegroundColorSpan extends ForegroundColorSpan {

    private float mAlpha = 1.f;

    public AlphaForegroundColorSpan(int color) {
        super(color);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(getAlphaColor());
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getAlpha() {
        return mAlpha;
    }

    private int getAlphaColor() {
        int foregroundColor = getForegroundColor();
        return Color.argb((int) (mAlpha * 255), Color.red(foregroundColor), Color.green(foregroundColor), Color.blue(foregroundColor));
    }
}