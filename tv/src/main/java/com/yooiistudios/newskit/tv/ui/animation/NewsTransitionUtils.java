package com.yooiistudios.newskit.tv.ui.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Point;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.yooiistudios.newskit.tv.ui.fragment.MainFragment;
import com.yooiistudios.newskit.tv.ui.fragment.NewsContentFragment;
import com.yooiistudios.newskit.core.ui.animation.AnimatorListenerImpl;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newskit.core.util.Display;
import com.yooiistudios.newskit.core.util.IntegerMath;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * DetailsTransitionUtils
 *  디테일 화면 애니메이션을 관리하는 클래스. 지금은 사용하지 않고 있음
 */
public class NewsTransitionUtils {
    private FrameLayout mContainerLayout;
    private ScrollView mScrollView;
    private ActivityTransitionProperty mTransitionProperty;

    private NewsTransitionUtils(NewsContentFragment fragment) {
        initViews(fragment);
        initTransitionProperty(fragment);
    }

    /*
    public static void runEnterAnimation(NewsDetailsContentFragment fragment) {
        new DetailsTransitionUtils(fragment).requestActivityTransition();
    }
    */

    private void initViews(NewsContentFragment fragment) {
        mContainerLayout = fragment.getLayout();
        mScrollView = fragment.getScrollView();
    }

    private void initTransitionProperty(NewsContentFragment fragment) {
        String transitionPropertyStr = fragment.getActivity().getIntent().getExtras().getString(
                MainFragment.TRANSITION_PROPERTY_ARG_KEY);
        mTransitionProperty = new Gson().fromJson(transitionPropertyStr, ActivityTransitionProperty.class);
    }

    private void requestActivityTransition() {
        mContainerLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                       int oldTop, int oldRight, int oldBottom) {
                mContainerLayout.removeOnLayoutChangeListener(this);
                mScrollView.setVisibility(View.INVISIBLE);
                revealBackground();
                startScrollViewAnimation();
            }
        });
    }

    private void revealBackground() {
        TimeInterpolator fastOutSlowInInterpolator =
                AnimationUtils.loadInterpolator(mContainerLayout.getContext(),
                        android.R.interpolator.fast_out_slow_in);

        Animator animator = ViewAnimationUtils.createCircularReveal(
                mContainerLayout, getRevealCenter().x, getRevealCenter().y,
                getRevealStartRadius(), getRevealTargetRadius());
        animator.setInterpolator(fastOutSlowInInterpolator);
        animator.setDuration(450);
        animator.start();
    }

    private void startScrollViewAnimation() {
        TimeInterpolator fastOutSlowInInterpolator =
                AnimationUtils.loadInterpolator(mScrollView.getContext(),
                        android.R.interpolator.fast_out_slow_in);

        ObjectAnimator scrollViewAnimator = ObjectAnimator.ofFloat(mScrollView, "translationY",
                Display.getDisplaySize(mScrollView.getContext()).y * 1.0f, 0);
        scrollViewAnimator.setStartDelay(280);
        scrollViewAnimator.setDuration(450);
        scrollViewAnimator.setInterpolator(fastOutSlowInInterpolator);
        scrollViewAnimator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mScrollView.setVisibility(View.VISIBLE);
            }
        });
        scrollViewAnimator.start();
    }

    private Point getRevealCenter() {
        return mTransitionProperty.getCenter();
    }

    private int getRevealStartRadius() {
        return Math.min(mTransitionProperty.getWidth(), mTransitionProperty.getHeight()) / 2;
    }

    private int getRevealTargetRadius() {
        return getFarthestLengthFromRevealCenterToRevealCorner();
    }

    private int getFarthestLengthFromRevealCenterToRevealCorner() {
        Point center = getRevealCenter();
        int distanceToRevealViewLeft = center.x - mContainerLayout.getLeft();
        int distanceToRevealViewTop = center.y - mContainerLayout.getTop();
        int distanceToRevealViewRight = mContainerLayout.getRight() - center.x;
        int distanceToRevealViewBottom = mContainerLayout.getBottom() - center.y;

        int distanceToRevealLeftTop =
                (int)Math.hypot(distanceToRevealViewLeft, distanceToRevealViewTop);
        int distanceToRevealRightTop =
                (int)Math.hypot(distanceToRevealViewRight, distanceToRevealViewTop);
        int distanceToRevealRightBottom =
                (int)Math.hypot(distanceToRevealViewRight, distanceToRevealViewBottom);
        int distanceToRevealLeftBottom =
                (int)Math.hypot(distanceToRevealViewLeft, distanceToRevealViewBottom);

        return IntegerMath.getLargestInteger(distanceToRevealLeftTop,
                distanceToRevealRightTop,
                distanceToRevealRightBottom, distanceToRevealLeftBottom);
    }
}
