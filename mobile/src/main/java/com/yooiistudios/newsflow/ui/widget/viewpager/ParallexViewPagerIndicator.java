package com.yooiistudios.newsflow.ui.widget.viewpager;

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

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.util.DipToPixel;
import com.yooiistudios.newsflow.ui.animation.AnimationFactory;

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

    private static final int INDICATOR_WIDTH_DP = 85;
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
                DipToPixel.dpToPixel(getContext(), INDICATOR_WIDTH_DP),
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
            int transitionX = ((getWidth() - mIndicatorView.getWidth()) / (mCount - 1)) * position;
            ObjectAnimator translationAnim =
                    ObjectAnimator.ofFloat(mIndicatorView, "translationX", transitionX);
            translationAnim.setDuration(ANIMATION_DURATION);
            translationAnim.setInterpolator(
                    AnimationFactory.makeDefaultReversePathInterpolator());
            translationAnim.start();
        }
    }

    public void setPage(final int position) {
        if (mCount > 0) {
            mIndicatorView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mIndicatorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (mViewPager.getAdapter() != null) {
                        int transitionX = ((getWidth() - mIndicatorView.getWidth()) / (mCount - 1))
                                * position;
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
