package com.yooiistudios.news.ui.widget.viewpager;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 18.
 *
 *  SlowSpeedScroller
 *   뷰페이저의 속도를 늦추기 위해 사용
 */
public class SlowSpeedScroller extends Scroller {
    public static int SWIPE_DURATION = 375;

    public SlowSpeedScroller(Context context) {
        super(context);
    }

    public SlowSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public SlowSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, SWIPE_DURATION);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, SWIPE_DURATION);
    }
}
