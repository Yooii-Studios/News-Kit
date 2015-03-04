package com.yooiistudios.newsflow.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 20.
 *
 * CoverFrameLayout
 *  메인화면 편집 모드에서 사용될 커버 레이아웃
 */
public class CoverFrameLayout extends FrameLayout {
    public CoverFrameLayout(Context context) {
        super(context);
    }

    public CoverFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CoverFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
