package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
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

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d("CustomRefreshLayout", "Action Down");
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
//                Log.d("CustomRefreshLayout", "Action Move");
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
//                Log.d("CustomRefreshLayout", "X velocity: " +
//                        VelocityTrackerCompat.getXVelocity(mVelocityTracker,
//                                pointerId));
//                Log.d("CustomRefreshLayout", "Y velocity: " +
//                        VelocityTrackerCompat.getYVelocity(mVelocityTracker,
//                                pointerId));
                if (VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                        pointerId) < 2500 && !isPullVelocityEnoughToRefresh) {
//                    Log.d("CustomRefreshLayout", "velocity is lower than 200 or boolean is false");
                    return true;
                } else {
                    Log.d("CustomRefreshLayout", "Y velocity: " +
                            VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                                    pointerId));
                    Log.d("CustomRefreshLayout", "velocity boolean is true");
                    isPullVelocityEnoughToRefresh = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d("CustomRefreshLayout", "Action Cancel/Up");
//                mVelocityTracker.recycle();
                isPullVelocityEnoughToRefresh = false;
                break;
        }
        return super.onTouchEvent(event);
    }
    */
}
