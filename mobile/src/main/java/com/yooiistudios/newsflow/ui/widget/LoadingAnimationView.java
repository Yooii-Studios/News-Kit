package com.yooiistudios.newsflow.ui.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.ui.animation.AnimatorListenerImpl;
import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.core.util.IntegerMath;
import com.yooiistudios.newsflow.ui.animation.AnimationFactory;

import io.codetail.animation.SupportAnimator;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 23.
 *
 * LoadingAnimationView
 *  첫 로딩 애니메이션을 구현한 뷰
 */
public class LoadingAnimationView extends FrameLayout implements LoadingCircleProgressView.CircleAnimationListener {
    private static final int PANEL_ANIM_DURATION = 400;
    private static final int PANEL_ANIM_START_DELAY = 180;
    private static final float FADE_ALPHA = 0.15f;
    private static final int CIRCLE_ANIM_START_DELAY = 380;
    private static final int CIRCLE_ANIM_START_DELAY_LOLLIPOP = 550;
    private static final int CIRCLE_SCALE_UP_ANIM_DURATION = 140;
    private static final int CIRCLE_SCALE_DOWN_ANIM_DURATION = 190;
    private static final int REVEAL_ANIM_START_DELAY = 140;
    private static final int REVEAL_ANIM_DURATION = 520;
    private static final int BACKGROUND_FADE_ANIM_DURATION = 800;

    private LinearLayout mPanelLayout;
    private View mTopView;
    private View mBottomView1; // 좌상단
    private View mBottomView2; // 우상단
    private View mBottomView3; // 좌하단
    private View mBottomView4; // 우하단
    private LoadingCircleProgressView mCircleProcessView;
    private View mRevealView;

    private boolean mIsAnimating = false;
    private boolean mNeedToFinishPanelAnimation = false;

    Handler mCircleAnimHandler = new Handler();
    private LoadingAnimListener mListener;

    public interface LoadingAnimListener {
        void onBackgroundFadeOutAnimationEnd();
    }

    public LoadingAnimationView(Context context) {
        super(context);
        init();
    }

    public LoadingAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.loading_anim_view, this);

        mPanelLayout = (LinearLayout) findViewById(R.id.loading_panel_layout);

        mTopView = findViewById(R.id.loading_anim_top_view);
        mBottomView1 = findViewById(R.id.loading_anim_bottom_view1);
        mBottomView2 = findViewById(R.id.loading_anim_bottom_view2);
        mBottomView3 = findViewById(R.id.loading_anim_bottom_view3);
        mBottomView4 = findViewById(R.id.loading_anim_bottom_view4);

        mCircleProcessView = (LoadingCircleProgressView) findViewById(R.id.loading_circle_view);

        mRevealView = findViewById(R.id.loading_reveal_view);
        mRevealView.setVisibility(View.GONE);

        setClipChildren(false);
        setBackgroundColor(getResources().getColor(R.color.material_light_blue_A700));

        // 클릭 방지
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startPanelAnimation(mListener);
    }

    /*
    public boolean isAnimating() {
        return mIsAnimating;
    }
    */

    public void startPanelAnimation(@Nullable LoadingAnimListener listener) {
        mListener = listener;

        if (!mIsAnimating) {
            mIsAnimating = true;
            startTopViewAnimation();
            startBottomView1Animation();
            startBottomView2Animation();
            startBottomView3Animation();
            startBottomView4Animation();
        }
    }

    public void startCircleAnimation(@Nullable LoadingAnimListener listener) {
        mListener = listener;

        if (!mIsAnimating) {
            mIsAnimating = true;

            int delay;
            if (Device.hasLollipop()) {
                delay = CIRCLE_ANIM_START_DELAY_LOLLIPOP;
            } else {
                delay = CIRCLE_ANIM_START_DELAY;
            }

            mCircleAnimHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCircleProcessView.startCircleAnimation(LoadingAnimationView.this);
                }
            }, delay);
        }
    }

    public void stopPanelAnimationAndStartArcAnimation() {
        mNeedToFinishPanelAnimation = true;
    }

    private void startTopViewAnimation() {
        startAlphaAnimator(mTopView, 0, false);
    }

    private void startBottomView1Animation() {
        startAlphaAnimator(mBottomView1, PANEL_ANIM_START_DELAY, false);
    }

    private void startBottomView2Animation() {
        startAlphaAnimator(mBottomView2, PANEL_ANIM_START_DELAY * 2, false);
    }

    private void startBottomView3Animation() {
        startAlphaAnimator(mBottomView3, PANEL_ANIM_START_DELAY * 3, false);
    }

    private void startBottomView4Animation() {
        startAlphaAnimator(mBottomView4, PANEL_ANIM_START_DELAY * 4, true);
    }

    private void startAlphaAnimator(final View target, int startDelay, final boolean needCallback) {
        ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(target, "alpha", 1.f, FADE_ALPHA);
        fadeInAnim.setDuration(PANEL_ANIM_DURATION);
        fadeInAnim.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(target, "alpha", FADE_ALPHA, 1.f);
                fadeOutAnim.setDuration(PANEL_ANIM_DURATION);
                if (needCallback && !mNeedToFinishPanelAnimation) {
                    fadeOutAnim.addListener(new AnimatorListenerImpl() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mIsAnimating = false;
                            startPanelAnimation(mListener);
                        }
                    });
                } else if (needCallback) {
                    fadeOutAnim.addListener(new AnimatorListenerImpl() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            stopPanelAnimationAndStartArcAnimation();
                            mCircleProcessView.startCircleAnimation(LoadingAnimationView.this);
                        }
                    });
                }
                fadeOutAnim.start();
            }
        });
        fadeInAnim.setStartDelay(startDelay);
        fadeInAnim.start();
    }


    @Override
    public void onCircleAnimationEnd() {
        startScaleAnimation();
    }

    private void startScaleAnimation() {
        // 살짝 커졌다가 안보일때까지 줄어들어야 함 -> 이후 revealAnimation 실행
        ValueAnimator circleScaleUpAnim = createCircleScaleUpAnimation();
        circleScaleUpAnim.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ValueAnimator circleScaleDownAnim = createCircleScaleDownAnimation();
                circleScaleDownAnim.addListener(new AnimatorListenerImpl() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startRevealAnimation();
                    }
                });
                circleScaleDownAnim.start();
            }
        });
        circleScaleUpAnim.start();
    }

    private ValueAnimator createCircleScaleUpAnimation() {
        int originalWidth = mCircleProcessView.getWidth();
        int targetWidth = (int) (mCircleProcessView.getWidth() * 1.08f);

        ValueAnimator animator = ValueAnimator.ofInt(originalWidth, targetWidth);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedViewSize = (int) animation.getAnimatedValue();
                mCircleProcessView.invalidateWidthCircleSize(animatedViewSize);
            }
        });

        animator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        animator.setDuration(CIRCLE_SCALE_UP_ANIM_DURATION);
        return animator;
    }

    private ValueAnimator createCircleScaleDownAnimation() {
        int targetWidth = (int) (mCircleProcessView.getWidth() * 1.08f);
        ValueAnimator circleScaleDownAnim = ValueAnimator.ofInt(targetWidth, 0);
        circleScaleDownAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedViewSize = (int) animation.getAnimatedValue();
                mCircleProcessView.invalidateWidthCircleSize(animatedViewSize);
            }
        });
        circleScaleDownAnim.setDuration(CIRCLE_SCALE_DOWN_ANIM_DURATION);
        circleScaleDownAnim.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        return circleScaleDownAnim;
    }

    private void startRevealAnimation() {
        // 하얀색으로 화면을 가득 채워야함
        mCircleProcessView.setVisibility(View.GONE);
        mPanelLayout.setVisibility(View.GONE);

        if (Device.hasLollipop()) {
            revealBackgroundAfterLollipop();
        } else {
            revealBackgroundBeforeLollipop();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealBackgroundAfterLollipop() {
        Animator animator = ViewAnimationUtils.createCircularReveal(mRevealView,
                getRevealCenter().x, getRevealCenter().y, 0, getRevealTargetRadius());
        animator.setStartDelay(REVEAL_ANIM_START_DELAY);
        animator.setDuration(REVEAL_ANIM_DURATION);
        animator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        animator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRevealView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startBackgroundFadeOutAnimation();
            }
        });
        // detach 된 뷰를 animating 할 때 크래시 방지
        if (mRevealView.isAttachedToWindow()) {
            animator.start();
        }
    }

    private void revealBackgroundBeforeLollipop() {
        SupportAnimator animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                mRevealView, getRevealCenter().x, getRevealCenter().y, 0, getRevealTargetRadius());
        animator.setDuration(REVEAL_ANIM_DURATION);

        android.view.animation.Interpolator interpolator = (android.view.animation.Interpolator)
                AnimationFactory.createFastOutSlowInInterpolator();
        animator.setInterpolator(interpolator);

        // 강제로 캐스팅해서 startDelay 를 주어야 하는듯
        com.nineoldandroids.animation.Animator innerAnimator =
                (com.nineoldandroids.animation.Animator) animator.get();
        innerAnimator.setStartDelay(REVEAL_ANIM_START_DELAY);

        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                mRevealView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel() {}

            @Override
            public void onAnimationRepeat() {}

            @Override
            public void onAnimationEnd() {
                startBackgroundFadeOutAnimation();
            }
        });
        // detach 된 뷰를 animating 할 때 크래시 방지
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mRevealView.isAttachedToWindow()) {
                animator.start();
            }
        } else {
            animator.start();
        }
    }

    private Point getRevealCenter() {
        return new Point(getWidth() / 2, getHeight() / 2);
    }

    private int getRevealTargetRadius() {
        return getFarthestLengthFromRevealCenterToRevealCorner();
    }

    private int getFarthestLengthFromRevealCenterToRevealCorner() {
        Point center = getRevealCenter();
        int distanceToRevealViewLeft = center.x - mRevealView.getLeft();
        int distanceToRevealViewTop = center.y - mRevealView.getTop();
        int distanceToRevealViewRight = mRevealView.getRight() - center.x;
        int distanceToRevealViewBottom = mRevealView.getBottom() - center.y;

        int distanceToRevealLeftTop =
                (int) Math.hypot(distanceToRevealViewLeft, distanceToRevealViewTop);
        int distanceToRevealRightTop =
                (int) Math.hypot(distanceToRevealViewRight, distanceToRevealViewTop);
        int distanceToRevealRightBottom =
                (int) Math.hypot(distanceToRevealViewRight, distanceToRevealViewBottom);
        int distanceToRevealLeftBottom =
                (int) Math.hypot(distanceToRevealViewLeft, distanceToRevealViewBottom);

        return IntegerMath.getLargestInteger(distanceToRevealLeftTop,
                distanceToRevealRightTop,
                distanceToRevealRightBottom, distanceToRevealLeftBottom);
    }

    private void startBackgroundFadeOutAnimation() {
        ValueAnimator animator = ObjectAnimator.ofFloat(this, "alpha", 1.f, 0.f);
        animator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        animator.setDuration(BACKGROUND_FADE_ANIM_DURATION);
        animator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsAnimating = false;
                mNeedToFinishPanelAnimation = false;
                if (mListener != null) {
                    mListener.onBackgroundFadeOutAnimationEnd();
                    mListener = null;
                }
                ((ViewGroup) getParent()).removeView(LoadingAnimationView.this);
            }
        });
        animator.start();
    }
}
