package com.yooiistudios.newsflow.ui.animation;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.os.Build;
import android.widget.ImageView;

import com.yooiistudios.newsflow.core.util.Device;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 25.
 *
 * ArgbObjectAnimator
 *  롤리팝 이전에서 ObjectAnimator.ofArgb 를 사용하기 위한 클래스
 */
public class ImageFilterAnimator {
    // DOUBT 파라미터가 많은데 팩토리로 빼야 하나?
    public static void animate(final ImageView imageView, int startArgb,
                               int endArgb, long duration, long startOffset,
                               Animator.AnimatorListener listener) {
        imageView.setColorFilter(startArgb);

        ValueAnimator animator;
        if (Device.hasLollipop()) {
            animator = createObjectAnimator(imageView.getColorFilter(), "color", endArgb);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    imageView.getDrawable().setColorFilter(imageView.getColorFilter());
                }
            });
        } else {
            animator = ValueAnimator.ofObject(new ArgbEvaluator(), startArgb, endArgb);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int animatedValue = (Integer) valueAnimator.getAnimatedValue();
                    imageView.getDrawable().setColorFilter(animatedValue, PorterDuff.Mode.SRC_ATOP);
                    imageView.setColorFilter(animatedValue, PorterDuff.Mode.SRC_ATOP);
                }
            });
        }
        animator.addListener(listener);
        animator.setStartDelay(startOffset);
        animator.setDuration(duration).start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ObjectAnimator createObjectAnimator(Object target, String propertyName, int... values) {
        return ObjectAnimator.ofArgb(target, propertyName, values);
    }
}
