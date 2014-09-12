package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 12.
 *
 * MainViewPager
 *  메인에서 상하 스크롤 감도를 체크하기 위해 사용될 리프레시 레이아웃
 */
public class MainRefreshLayout extends SwipeRefreshLayout {
    VelocityTracker mVelocityTracker;
    Boolean isPullVelocityEnoughToRefresh = false;

    public MainRefreshLayout(Context context) {
        super(context);
    }
    public MainRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                // y 속도가 900이 되지 않으면 리프레시를 시작하지 않는다
                if (VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId) < 900 &&
                        !isPullVelocityEnoughToRefresh) {
                    return true;
                } else {
                    // y 속도가 충분히 빨라도, 뷰페이저의 가로 스와이프일 수 있으므로 일정 x 속도 이하일 경우만 세로
                    // 스크롤로 인식하게 구현
                    float xVelocity = Math.abs(VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                    if (Math.abs(xVelocity) < 900) {
                        isPullVelocityEnoughToRefresh = true;
                    } else {
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mVelocityTracker.clear();
                isPullVelocityEnoughToRefresh = false;
                break;
        }
        return super.onTouchEvent(event);
    }
}
