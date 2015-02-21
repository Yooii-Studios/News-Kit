package com.yooiistudios.newsflow.ui.animation;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.Settings;
import com.yooiistudios.newsflow.util.InterpolatorHelper;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * AnimationFactory
 *  필요한 애니메이션을 제작해주는 클래스
 */
public class AnimationFactory {
    private static final int EDIT_LAYOUT_ANIM_DURATION = 2500;

    private AnimationFactory() { throw new AssertionError("You MUST not create this class!"); }

    public static Animation makeBottomFadeOutAnimation(Context context) {
        Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(getBottomDuration(context));
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation.setInterpolator(context, R.animator.interpolator_bottom_fade);
        } else {
            animation.setInterpolator(new CubicBezierInterpolator(.57f, .15f, .65f, .67f));
        }
        return animation;
    }

    public static Animation makeBottomFadeInAnimation(Context context) {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(getBottomDuration(context));
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation.setInterpolator(context, R.animator.interpolator_bottom_fade);
        } else {
            animation.setInterpolator(new CubicBezierInterpolator(.57f, .15f, .65f, .67f));
        }

        return animation;
    }

    public static ValueAnimator makeBottomFadeOutAnimator(Context context, View targetView) {
//        PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
//        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(holder);
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "alpha", 1.0f, 0.0f);
        animator.setDuration(getBottomDuration(context));
        animator.setInterpolator(getBottomInterpolator());

        return animator;
    }

    public static ValueAnimator makeBottomFadeInAnimator(Context context, View targetView) {
//        PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
//        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(holder);
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "alpha", 0.0f, 1.0f);
        animator.setDuration(getBottomDuration(context));
        animator.setInterpolator(getBottomInterpolator());

        return animator;
    }

    public static long getBottomDuration(Context context) {
        // 속도에 따라 duration 조절
        int originalDuration = context.getResources().getInteger(
                R.integer.bottom_news_feed_fade_anim_duration_milli);
        return (long) (originalDuration * Settings.getAutoRefreshSpeed(context));
    }

    private static Interpolator getBottomInterpolator() {
        return new CubicBezierInterpolator(.57f, .15f, .65f, .67f);
    }

    public static TimeInterpolator makeDefaultPathInterpolator() {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(.4f, .0f, 1.f, .2f);
        } else {
            interpolator = new CubicBezierInterpolator(.4f, .0f, 1.f, .2f);
        }
        return interpolator;
    }

    public static TimeInterpolator makeDefaultReversePathInterpolator() {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(.0f, .4f, .2f, 1.f);
        } else {
            interpolator = new CubicBezierInterpolator(.0f, .4f, .2f, 1.f);
        }
        return interpolator;
    }

    // slow-out-slow-in
    public static TimeInterpolator makeNewsFeedImageAndRootTransitionInterpolator(Context context) {
        return InterpolatorHelper.makeImageAndRootTransitionInterpolator(context);
    }

    public static TimeInterpolator makeNewsFeedImageScaleInterpolator(Context context) {
        return InterpolatorHelper.makeImageScaleInterpolator(context);
    }

    // fast-out-slow-in
    public static TimeInterpolator makeNewsFeedRootBoundHorizontalInterpolator(Context context) {
        return InterpolatorHelper.makeRootWidthScaleInterpolator(context);
    }

    // ease-in-out
    public static TimeInterpolator makeNewsFeedRootBoundVerticalInterpolator(Context context) {
        return InterpolatorHelper.makeRootHeightScaleInterpolator(context);
    }

    public static TimeInterpolator makeNewsFeedReverseTransitionInterpolator() {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(.52f, .22f, 1.f, .21f);
        } else {
            interpolator = new CubicBezierInterpolator(.52f, .22f, 1.f, .21f);
        }
        return interpolator;
    }

    public static TimeInterpolator makeViewPagerScrollInterpolator() {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(0.15f, 0.12f, 0.24f, 1.0f);
        } else {
            interpolator = new CubicBezierInterpolator(0.15f, 0.12f, 0.24f, 1.0f);
        }
        return interpolator;
    }

//    public static void animateEditLayoutFadeIn(final View viewToAnimate) {
//        viewToAnimate.setAlpha(0);
//        viewToAnimate.animate()
//                .setDuration(EDIT_LAYOUT_ANIM_DURATION)
//                .alpha(1)
//                .withStartAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        viewToAnimate.setVisibility(View.VISIBLE);
//                    }
//                });
//    }
//
//    public static void animateEditLayoutFadeOut(final View viewToAnimate) {
//        viewToAnimate.setAlpha(0);
//    }
}
