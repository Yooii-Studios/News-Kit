package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.yooiistudios.news.util.log.NLLog;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 31.
 *
 * SizeMeasureRelativeLayout
 *  자신의 사이즈를 계산하고 계산된 크기를 콜백으로 돌려줌
 */
public class SizeMeasureRelativeLayout extends RelativeLayout {

    private static final String TAG = SizeMeasureRelativeLayout.class.getName();

    private OnMeasureListener mOnMeasureListener;

    public interface OnMeasureListener {
        public void onMeasureSize(int measuredWidth, int measuredHeight);
    }

    public SizeMeasureRelativeLayout(Context context) {
        super(context);
    }

    public SizeMeasureRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizeMeasureRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SizeMeasureRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnMeasureListener(OnMeasureListener listener) {
        mOnMeasureListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        NLLog.i(TAG, "measuredWidth : " + measuredWidth);
        NLLog.i(TAG, "measuredHeight : " + measuredHeight);

        if (mOnMeasureListener != null) {
            mOnMeasureListener.onMeasureSize(measuredWidth, measuredHeight);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
