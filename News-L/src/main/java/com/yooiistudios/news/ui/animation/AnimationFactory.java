package com.yooiistudios.news.ui.animation;

import android.content.Context;
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
        fadeOutAnim.setInterpolator(context, R.animator.interpolator_bottom_fade);
        return fadeOutAnim;
    }

    public static Animation makeBottomFadeInAnimation(Context context) {
        Animation fadeOutAnim = new AlphaAnimation(0.0f, 1.0f);
        fadeOutAnim.setDuration(context.getResources().getInteger(R.integer.bottom_news_feed_fade_anim_duration_milli));
        fadeOutAnim.setFillEnabled(true);
        fadeOutAnim.setFillAfter(true);
        fadeOutAnim.setInterpolator(context, R.animator.interpolator_bottom_fade);
        return fadeOutAnim;
    }

    public static PathInterpolator makeDefaultPathInterpolator() {
        return new PathInterpolator(.4f, .0f, 1.f, .2f);
    }

    public static PathInterpolator makeDefaultReversePathInterpolator() {
        return new PathInterpolator(.0f, .4f, .2f, 1.f);
    }

    // slow-out-slow-in
    public static PathInterpolator makeNewsFeedImageAndRootTransitionInterpolator(Context context) {
        return InterpolatorHelper.makeImageAndRootTransitionInterpolator(context);
//
    }

    public static PathInterpolator makeNewsFeedImageScaleInterpolator(Context context) {
        return InterpolatorHelper.makeImageScaleInterpolator(context);
    }

    // fast-out-slow-in
    public static PathInterpolator makeNewsFeedRootBoundHorizontalInterpolator(Context context) {
        return InterpolatorHelper.makeRootWidthScaleInterpolator(context);
    }

    // ease-in-out
    public static PathInterpolator makeNewsFeedRootBoundVerticalInterpolator(Context context) {
        return InterpolatorHelper.makeRootHeightScaleInterpolator(context);
    }

    public static PathInterpolator makeNewsFeedReverseTransitionInterpolator() {
        return new PathInterpolator(.52f, .22f, 1.f, .21f);
    }

    public static PathInterpolator makeViewPagerScrollInterpolator() {
        return new PathInterpolator(0.15f, 0.12f, 0.24f, 1.0f);
    }
}
