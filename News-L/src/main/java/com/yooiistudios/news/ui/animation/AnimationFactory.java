package com.yooiistudios.news.ui.animation;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;

import com.yooiistudios.news.R;

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

    public static PathInterpolator makeNewsFeedTransitionInterpolator() {
        return new PathInterpolator(0.4f, 0.f, 0.2f, 1.f);
    }

    public static PathInterpolator makeNewsFeedRootBoundInterpolator() {
        return new PathInterpolator(.0f, .46f, .31f, 1.f);
    }

    public static PathInterpolator makeNewsFeedReverseTransitionInterpolator() {
        return new PathInterpolator(.52f, .22f, 1.f, .21f);
    }

    public static PathInterpolator makeViewPagerScrollInterpolator() {
        return new PathInterpolator(0.15f, 0.12f, 0.24f, 1.0f);
    }
}
