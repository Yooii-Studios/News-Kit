package com.yooiistudios.newskit.ui.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Point;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.yooiistudios.newskit.core.ui.animation.AnimatorListenerImpl;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newskit.core.util.Display;
import com.yooiistudios.newskit.core.util.IntegerMath;
import com.yooiistudios.newskit.ui.activity.PairActivity;
import com.yooiistudios.newskit.ui.fragment.MainFragment;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * PairTransitionUtils
 *  페어 화면의 애니메이션을 담당
 */
public class PairTransitionUtils {
    private FrameLayout mContainerLayout;
    private LinearLayout mDialogLayout;
    private ActivityTransitionProperty mTransitionProperty;
    private PairTransitionCallback mCallback;

    public interface PairTransitionCallback {
        public void onTransitionAnimationEnd();
    }

    private PairTransitionUtils(PairActivity activity, PairTransitionCallback callback) {
        mCallback = callback;
        initViews(activity);
        initTransitionProperty(activity);
    }

    public static void runEnterAnimation(PairActivity activity, PairTransitionCallback callback) {
        new PairTransitionUtils(activity, callback).requestActivityTransition();
    }

    private void initViews(PairActivity activity) {
        mContainerLayout = activity.getContainerLayout();
        mDialogLayout = activity.getDialogLayout();
    }

    private void initTransitionProperty(PairActivity activity) {
        String transitionPropertyStr = activity.getIntent().getExtras().getString(
                MainFragment.TRANSITION_PROPERTY_ARG_KEY);
        mTransitionProperty = new Gson().fromJson(transitionPropertyStr,
                ActivityTransitionProperty.class);
    }

    private void requestActivityTransition() {
        mContainerLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                       int oldTop, int oldRight, int oldBottom) {
                mContainerLayout.removeOnLayoutChangeListener(this);
                mDialogLayout.setVisibility(View.INVISIBLE);
                revealBackground();
                startDialogLayoutAnimation();
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

    private void startDialogLayoutAnimation() {
        TimeInterpolator fastOutSlowInInterpolator =
                AnimationUtils.loadInterpolator(mDialogLayout.getContext(),
                        android.R.interpolator.fast_out_slow_in);

        float previousY = mDialogLayout.getTranslationY();
        ObjectAnimator dialogLayoutAnimator = ObjectAnimator.ofFloat(mDialogLayout, "translationY",
                Display.getDisplaySize(mDialogLayout.getContext()).y, previousY);
        dialogLayoutAnimator.setStartDelay(250);
        dialogLayoutAnimator.setDuration(450);
        dialogLayoutAnimator.setInterpolator(fastOutSlowInInterpolator);
        dialogLayoutAnimator.addListener(new AnimatorListenerImpl() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mDialogLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mCallback != null) {
                    mCallback.onTransitionAnimationEnd();
                }
            }
        });
        dialogLayoutAnimator.start();
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
