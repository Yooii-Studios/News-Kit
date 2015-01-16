package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.yooiistudios.news.ui.adapter.MainBottomAdapter;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 20.
 *
 * NLSquareCardView
 *  카드뷰의 긴 변의 길이에 맞게 뷰의 크기를 변형해주는 클래스
 */
public class RectangleFrameLayout extends RatioFrameLayout {
    private static final String TAG = RectangleFrameLayout.class.getName();

    public RectangleFrameLayout(Context context) {
        super(context);
    }

    public RectangleFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangleFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected float getTargetAxisLength(float baseAxisLength) {
        if (getBaseAxis() == AXIS_WIDTH) {
            return MainBottomAdapter.getRowHeight(baseAxisLength);
        } else {
            return MainBottomAdapter.getRowWidth(baseAxisLength);
        }
    }
}
