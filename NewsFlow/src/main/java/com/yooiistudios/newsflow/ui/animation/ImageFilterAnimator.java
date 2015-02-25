package com.yooiistudios.newsflow.ui.animation;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
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
        imageView.setColorFilter(startArgb);
        ObjectAnimator animator;
        if (Device.hasLollipop()) {
            animator = createObjectAnimator(imageView.getColorFilter(), PROPERTY,
                    endArgb);
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    updateColorFilter(imageView);
//                }
//            });
//            animator.setDuration(duration).start();
        } else {
            animator =
                    createObjectAnimatorBeforeLollipop(imageView.getColorFilter(), PROPERTY,
                            endArgb);
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    updateColorFilter(imageView);
//                }
//            });
//            animator.setDuration(duration).start();
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateColorFilter(imageView);
            }
        });
        animator.setDuration(duration).start();
    }

    private static void updateColorFilter(ImageView imageView) {
        imageView.getDrawable().setColorFilter(imageView.getColorFilter());
    }

    private static ObjectAnimator createObjectAnimatorBeforeLollipop(
            Object target, String propertyName, int... values) {
        ObjectAnimator animator
                = ObjectAnimator.ofInt(target, propertyName, values);
        animator.setEvaluator(new ArgbEvaluator());

        return animator;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ObjectAnimator createObjectAnimator(
            Object target, String propertyName, int... values) {
        return ObjectAnimator.ofArgb(target, propertyName, values);
    }


    /**
     * Copied from NineOldAndroids library.
     */
//    private static class ArgbEvaluator implements TypeEvaluator {
//        public Object evaluate(float fraction, Object startValue, Object endValue) {
//            int startInt = (Integer) startValue;
//            int startA = (startInt >> 24);
//            int startR = (startInt >> 16) & 0xff;
//            int startG = (startInt >> 8) & 0xff;
//            int startB = startInt & 0xff;
//
//            int endInt = (Integer) endValue;
//            int endA = (endInt >> 24);
//            int endR = (endInt >> 16) & 0xff;
//            int endG = (endInt >> 8) & 0xff;
//            int endB = endInt & 0xff;
//
//            return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
//                    (int)((startR + (int)(fraction * (endR - startR))) << 16) |
//                    (int)((startG + (int)(fraction * (endG - startG))) << 8) |
//                    (int)((startB + (int)(fraction * (endB - startB))));
//        }
//    }
}
