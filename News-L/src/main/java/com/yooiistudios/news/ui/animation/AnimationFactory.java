package com.yooiistudios.news.ui.animation;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Build;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;

import com.yooiistudios.news.R;
import com.yooiistudios.news.util.InterpolatorHelper;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * AnimationFactory
 *  필요한 애니메이션을 제작해주는 클래스
 */
public class AnimationFactory {
    private AnimationFactory() { throw new AssertionError("You MUST not create this class!"); }

    public static Animation makeBottomFadeOutAnimation(Context context) {
        Animation fadeOutAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnim.setDuration(context.getResources().getInteger(R.integer.bottom_news_feed_fade_anim_duration_milli));
        fadeOutAnim.setFillEnabled(true);
        fadeOutAnim.setFillAfter(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fadeOutAnim.setInterpolator(context, R.animator.interpolator_bottom_fade);
        } else {
            fadeOutAnim.setInterpolator(new AccelerateDecelerateInterpolator(context, null));
        }
        return fadeOutAnim;
    }

    public static Animation makeBottomFadeInAnimation(Context context) {
        Animation fadeOutAnim = new AlphaAnimation(0.0f, 1.0f);
        fadeOutAnim.setDuration(context.getResources().getInteger(R.integer.bottom_news_feed_fade_anim_duration_milli));
        fadeOutAnim.setFillEnabled(true);
        fadeOutAnim.setFillAfter(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fadeOutAnim.setInterpolator(context, R.animator.interpolator_bottom_fade);
        } else {
            fadeOutAnim.setInterpolator(new AccelerateDecelerateInterpolator(context, null));
        }

        return fadeOutAnim;
    }

    public static TimeInterpolator makeDefaultPathInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(.4f, .0f, 1.f, .2f);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    public static TimeInterpolator makeDefaultReversePathInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(.0f, .4f, .2f, 1.f);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    // slow-out-slow-in
    public static TimeInterpolator makeNewsFeedImageAndRootTransitionInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = InterpolatorHelper.makeImageAndRootTransitionInterpolator(context);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    public static TimeInterpolator makeNewsFeedImageScaleInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = InterpolatorHelper.makeImageScaleInterpolator(context);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    // fast-out-slow-in
    public static TimeInterpolator makeNewsFeedRootBoundHorizontalInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = InterpolatorHelper.makeRootWidthScaleInterpolator(context);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    // ease-in-out
    public static TimeInterpolator makeNewsFeedRootBoundVerticalInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = InterpolatorHelper.makeRootHeightScaleInterpolator(context);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    public static TimeInterpolator makeNewsFeedReverseTransitionInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(.52f, .22f, 1.f, .21f);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }

    public static TimeInterpolator makeViewPagerScrollInterpolator(Context context) {
        TimeInterpolator interpolator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            interpolator = new PathInterpolator(0.15f, 0.12f, 0.24f, 1.0f);
        } else {
            interpolator = new AccelerateDecelerateInterpolator(context, null);
        }
        return interpolator;
    }
}
