package com.yooiistudios.newskit.ui.widget.viewpager;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.util.DipToPixel;
import com.yooiistudios.newskit.ui.animation.AnimationFactory;

/**
 * Created by Wooseong Kim in ViewPagerIndicatorTest from Yooii Studios Co., LTD. on 14. 11. 4.
 *
 * ThinViewPagerIndicator
 *  얇은 인디케이터
 */
public class ParallexViewPagerIndicator extends RelativeLayout implements ViewPager.OnPageChangeListener{

    private View mIndicatorView;
    private int mCount;
    private ViewPager mViewPager;

    private static final int ANIMATION_DURATION = 250;

    public ParallexViewPagerIndicator(Context context) {
        super(context);
    }

    public ParallexViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallexViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParallexViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr,
                                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initialize(int count, ViewPager viewPager) {
        mCount = count;

        // indicator
        if (mIndicatorView != null) {
            removeView(mIndicatorView);
        }
        mIndicatorView = new View(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                getContext().getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_indicator_width),
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        mIndicatorView.setLayoutParams(layoutParams);
        mIndicatorView.setBackgroundColor(getResources().getColor(R.color.main_top_indicator));
        addView(mIndicatorView);

        setViewPager(viewPager);
    }

    private void setViewPager(ViewPager viewPager) {
        if (mViewPager == viewPager) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (mCount > 0) {
            // 나누는 부분에 float 처리를 해 줘야 끝이 버려지지 않아 맨 끝 스크롤 시 아귀가 잘 맞음
            float transitionX = ((getWidth() - mIndicatorView.getWidth()) / (float)(mCount - 1)
                     * position);
            ObjectAnimator translationAnim =
                    ObjectAnimator.ofFloat(mIndicatorView, "translationX", transitionX);
            translationAnim.setDuration(ANIMATION_DURATION);
            translationAnim.setInterpolator(
                    AnimationFactory.makeDefaultReversePathInterpolator());
            translationAnim.start();
        }
    }

    public void setPage(final int position) {
        if (mCount > 0 && position > 0) {
            mIndicatorView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mIndicatorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (mViewPager.getAdapter() != null) {
                        // 나누는 부분에 float 처리를 해 줘야 끝이 버려지지 않아 맨 끝 스크롤 시 아귀가 잘 맞음
                        float transitionX = ((getWidth() - mIndicatorView.getWidth()) /
                                (float)(mCount - 1) * position);
                        mIndicatorView.setTranslationX(transitionX);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
