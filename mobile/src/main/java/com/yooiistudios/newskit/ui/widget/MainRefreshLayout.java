package com.yooiistudios.newskit.ui.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 12.
 *
 * MainViewPager
 *  메인에서 상하 스크롤 감도를 체크하기 위해 사용될 리프레시 레이아웃
 */
public class MainRefreshLayout extends SwipeRefreshLayout {
    private int mTouchSlop;
    private float mPrevX;

    public MainRefreshLayout(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    public MainRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    // slop 은 출렁거림을 뜻한다.
    // 안드로이드에서 touchSlop 은 touch 할때 출렁거림(slop) 의 정도를 이야기 한다.
    // touchSlop 을 정의해 줌으로써 의도하지 않은 scrolling 을 줄이는 역할을 한다.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float deltaX = Math.abs(eventX - mPrevX);

                // X의 변화량이 slop 보다 크면 하위 뷰에서 터치를 처리하도록 지시
                if (deltaX > mTouchSlop) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }
}
