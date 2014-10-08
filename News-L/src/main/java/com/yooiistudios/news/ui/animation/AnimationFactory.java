package com.yooiistudios.news.ui.animation;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * AnimationFactory
 *  필요한 애니메이션을 제작해주는 클래스
 */
public class AnimationFactory {
    private AnimationFactory() { throw new AssertionError("You MUST not create this class!"); }

    private static final int NEWS_FEED_ANIMATION_DURATION = 500;
//    private static final int NEWS_FEED_ANIMATION_FADE_DURATION = 420; // 260;
    private static final int NEWS_FEED_ANIMATION_FADE_DURATION = 750; // 260;
//    private static final int NEWS_FEED_ANIMATION_IMAGE_FADE_DURATION = 500;

    public static AnimationSet makeBottomSlideOutAnimation() {
        AnimationSet hideSet = new AnimationSet(true);
        hideSet.setInterpolator(new AccelerateInterpolator());

        Animation moveUpAnim = new TranslateAnimation
                (Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, -0.1f);
        moveUpAnim.setDuration(NEWS_FEED_ANIMATION_DURATION);
        moveUpAnim.setFillEnabled(true);
        moveUpAnim.setFillAfter(true);

        hideSet.addAnimation(moveUpAnim);

        Animation fadeoutAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeoutAnim.setDuration(NEWS_FEED_ANIMATION_FADE_DURATION);
        fadeoutAnim.setFillEnabled(true);
        fadeoutAnim.setFillAfter(true);
        hideSet.addAnimation(fadeoutAnim);
        return hideSet;
    }

    public static AnimationSet makeBottomSlideInAnimation() {
        AnimationSet showSet = new AnimationSet(false);
        showSet.setInterpolator(new DecelerateInterpolator());

        Animation moveDownAnim = new TranslateAnimation
                (Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.1f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
        moveDownAnim.setDuration(NEWS_FEED_ANIMATION_DURATION);
        moveDownAnim.setFillEnabled(true);
        moveDownAnim.setFillAfter(true);

        showSet.addAnimation(moveDownAnim);

        Animation fadeInAnim = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnim.setDuration(NEWS_FEED_ANIMATION_FADE_DURATION);
        fadeInAnim.setFillEnabled(true);
        fadeInAnim.setFillAfter(true);
        showSet.addAnimation(fadeInAnim);
        return showSet;
    }


    public static Animation makeBottomFadeOutAnimation() {
        Animation fadeOutAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnim.setDuration(NEWS_FEED_ANIMATION_FADE_DURATION);
        fadeOutAnim.setFillEnabled(true);
        fadeOutAnim.setFillAfter(true);
        fadeOutAnim.setInterpolator(makeDefaultPathInterpolator());
        return fadeOutAnim;
    }

    public static Animation makeBottomFadeInAnimation() {
        Animation fadeOutAnim = new AlphaAnimation(0.0f, 1.0f);
        fadeOutAnim.setDuration(NEWS_FEED_ANIMATION_FADE_DURATION);
        fadeOutAnim.setFillEnabled(true);
        fadeOutAnim.setFillAfter(true);
        fadeOutAnim.setInterpolator(new PathInterpolator(0.2f, 0.f, 0.4f, 1.f));
        return fadeOutAnim;
    }

    public static PathInterpolator makeDefaultPathInterpolator() {
        return new PathInterpolator(.4f, .0f, 1.f, .2f);
    }

    public static PathInterpolator makeDefaultReversePathInterpolator() {
        return new PathInterpolator(.0f, .4f, .2f, 1.f);
    }

    public static PathInterpolator makeNewsFeedTransitionInterpolator() {
        return new PathInterpolator(.22f, .52f, .21f, 1.f);
    }

    public static PathInterpolator makeNewsFeedReverseTransitionInterpolator() {
        return new PathInterpolator(.52f, .22f, 1.f, .21f);
    }

    public static PathInterpolator makeViewPagerScrollInterpolator() {
        return new PathInterpolator(0.15f, 0.12f, 0.24f, 1.0f);
    }
}
