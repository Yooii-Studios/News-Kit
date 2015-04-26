package com.yooiistudios.newskit.ui.widget;

import android.content.Context;

import com.yooiistudios.newskit.R;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 2. 11.
 *
 * ScrollDetector
 *  웹뷰의 스크롤 값을 계산해서 위 아래 방향 콜백을 해 주는 클래스
 */
public class ScrollDetector {
    private int mLastScrollY;
    private int mScrollThreshold;
    private OnScrollDirectionListener mListener;

    public interface OnScrollDirectionListener {
        void onScrollUp();
        void onScrollDown();
    }

    public ScrollDetector(Context context, OnScrollDirectionListener listener) {
        mListener = listener;
        mScrollThreshold = context.getResources().getDimensionPixelSize(R.dimen.fab_scroll_threshold);
    }

    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        boolean isSignificantDelta = Math.abs(t - mLastScrollY) > mScrollThreshold;
        if (isSignificantDelta) {
            if (mListener != null) {
                if (t > mLastScrollY) {
                    mListener.onScrollUp();
                } else {
                    mListener.onScrollDown();
                }
            }
        }
        mLastScrollY = t;
    }
}