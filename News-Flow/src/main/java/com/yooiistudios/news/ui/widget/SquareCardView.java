package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.yooiistudios.news.ui.adapter.MainBottomAdapter;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 20.
 *
 * NLSquareCardView
 *  카드뷰의 긴 변의 길이에 맞게 뷰의 크기를 변형해주는 클래스
 */
public class SquareCardView extends FrameLayout {
    public SquareCardView(Context context) {
        super(context);
    }

    public SquareCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int rowHeight = (int)MainBottomAdapter.getRowHeight(measuredWidth);

        int newWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        int newHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY);

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        setMeasuredDimension(measuredWidth, rowHeight);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureChild(getChildAt(i), newWidthMeasureSpec,
                    newHeightMeasureSpec);
        }

    }

}
