package com.yooiistudios.newskit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 16.
 *
 * RatioFrameLayout
 *  기준 축(가로 혹은 세로)과 비율을 가지고 나머지 축의 길이를 정하는 레이아웃
 */
public abstract class RatioFrameLayout extends FrameLayout {
    public static final int AXIS_WIDTH = 0;
    public static final int AXIS_HEIGHT = 1;

    private int mBaseAxis = AXIS_WIDTH;
    private boolean mIgnoreHeightAdjustment = false;

    public RatioFrameLayout(Context context) {
        super(context);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected abstract float getTargetAxisLength(float baseAxisLength);

    @IntDef(value = { AXIS_WIDTH, AXIS_HEIGHT })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Axis {}
    public void setBaseAxis(@Axis int axis) {
        mBaseAxis = axis;
    }

    public @Axis int getBaseAxis() {
        return mBaseAxis;
    }

    public void setIgnoreHeightAdjustment(boolean ignoreHeightAdjustment) {
        mIgnoreHeightAdjustment = ignoreHeightAdjustment;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mIgnoreHeightAdjustment) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int measuredWidth;
        int measuredHeight;

        if (mBaseAxis == AXIS_WIDTH) {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
//            rowHeight = (int) MainBottomAdapter.getRowHeight(measuredWidth);
            measuredHeight = (int) getTargetAxisLength(measuredWidth);
        } else {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
            measuredWidth = (int) getTargetAxisLength(measuredHeight);
        }

        int newWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        int newHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureChild(getChildAt(i), newWidthMeasureSpec,
                    newHeightMeasureSpec);
        }
    }
}
