package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.yooiistudios.news.util.log.NLLog;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 20.
 *
 * NLSquareCardView
 *  카드뷰의 긴 변의 길이에 맞게 뷰의 크기를 변형해주는 클래스
 */
public class NLSquareCardView extends CardView {
    private static final String TAG = NLSquareCardView.class.getName();

    public NLSquareCardView(Context context) {
        super(context);
    }

    public NLSquareCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        asdf(context, attrs);
    }

    public NLSquareCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        asdf(context, attrs);
    }

    private void asdf(Context context, AttributeSet attrs) {
        int[] layoutParamsArr = new int[]
                {android.R.attr.layout_width, android.R.attr.layout_height};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                layoutParamsArr);

        int widthParam = typedArray.getInt(0, 0);
        int heightParam = typedArray.getInt(1, 0);

        NLLog.i(TAG, "ViewGroup.LayoutParams.WRAP_CONTENT : " +
                ViewGroup.LayoutParams.WRAP_CONTENT);
        NLLog.i(TAG, "ViewGroup.LayoutParams.MATCH_PARENT : " +
                ViewGroup.LayoutParams.MATCH_PARENT);

        NLLog.i(TAG, "widthParam : " + widthParam);
        NLLog.i(TAG, "heightParam : " + heightParam);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        NLLog.i(TAG, "MeasureSpec.AT_MOST : " + MeasureSpec.AT_MOST); // wrap_content
        NLLog.i(TAG, "MeasureSpec.EXACTLY : " + MeasureSpec.EXACTLY); // match_parent
        NLLog.i(TAG, "MeasureSpec.UNSPECIFIED : " + MeasureSpec.UNSPECIFIED);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        NLLog.i(TAG, "widthMode : " + widthMode);
        NLLog.i(TAG, "heightMode : " + heightMode);

        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        NLLog.i(TAG, "measuredWidth : " + measuredWidth);
        NLLog.i(TAG, "measuredHeight : " + measuredHeight);

        int size = Math.max(measuredWidth, measuredHeight);
//        if (widthMode == MeasureSpec.EXACTLY &&
//                heightMode == MeasureSpec.EXACTLY) {
//            // if both are match_parent, use smaller one
//            size = Math.max(measuredWidth, measuredHeight);
//        }
//        else if (widthMode == MeasureSpec.EXACTLY) {
//
//        }

        int newWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        int newHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        setMeasuredDimension(size, size);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureChild(getChildAt(i), newWidthMeasureSpec,
                    newHeightMeasureSpec);
        }

    }

}
