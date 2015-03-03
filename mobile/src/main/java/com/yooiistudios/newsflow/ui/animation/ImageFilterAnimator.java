package com.yooiistudios.newsflow.ui.animation;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.os.Build;
import android.widget.ImageView;

import com.yooiistudios.newsflow.util.Device;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 25.
 *
 * ArgbObjectAnimator
 *  롤리팝 이전에서 ObjectAnimator.ofArgb 를 사용하기 위한 클래스
 */
public class ImageFilterAnimator {
    private static final String PROPERTY = "color";

    // DOUBT 파라미터가 많은데 팩토리로 빼야 하나?
    public static void animate(final ImageView imageView, int startArgb,
                               int endArgb, long duration) {
        ValueAnimator animator;
        if (Device.hasLollipop()) {
            imageView.setColorFilter(startArgb);
            animator = createObjectAnimator(imageView.getColorFilter(), PROPERTY, endArgb);
            animator.addUpdateListener(ColorFilterListener.create(imageView));
        } else {
            animator = ValueAnimator.ofObject(new ArgbEvaluator(), startArgb, endArgb);
            animator.addUpdateListener(ColorFilterListener.createUsingAnimatedValue(imageView));
        }
        animator.setDuration(duration).start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ObjectAnimator createObjectAnimator(Object target, String propertyName, int... values) {
        return ObjectAnimator.ofArgb(target, propertyName, values);
    }

    private static class ColorFilterListener implements ValueAnimator.AnimatorUpdateListener {
        private final ImageView mHeroImageView;
        private boolean mUseAnimatedValue = false;

        private ColorFilterListener(ImageView hero) {
            mHeroImageView = hero;
        }

        private ColorFilterListener(ImageView hero, boolean useAnimatedValue) {
            mHeroImageView = hero;
            mUseAnimatedValue = useAnimatedValue;
        }

        public static ColorFilterListener createUsingAnimatedValue(ImageView imageView) {
            return new ColorFilterListener(imageView, true);
        }

        public static ColorFilterListener create(ImageView imageView) {
            return new ColorFilterListener(imageView);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (mUseAnimatedValue) {
                int animatedValue = (Integer)valueAnimator.getAnimatedValue();
                mHeroImageView.getDrawable().setColorFilter(animatedValue, PorterDuff.Mode.SRC_ATOP);
            } else {
                mHeroImageView.getDrawable().setColorFilter(mHeroImageView.getColorFilter());
            }
        }
    }
}
