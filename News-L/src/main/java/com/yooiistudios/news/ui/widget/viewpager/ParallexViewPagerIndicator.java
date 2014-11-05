package com.yooiistudios.news.ui.widget.viewpager;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.util.DipToPixel;

import java.util.Random;

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

    private int mDeviceWidth;
    private int mIndicatorViewWidth;
    private static final int INDICATOR_WIDTH_DP = 100;
    private static final int ANIMATION_DURATION = 200;

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
    public ParallexViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initialize(int count, ViewPager viewPager) {
        mCount = count;

        // device
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mDeviceWidth = displayMetrics.widthPixels;

        // indicator
        mIndicatorView = new View(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                DipToPixel.dpToPixel(getContext(), INDICATOR_WIDTH_DP),
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        mIndicatorView.setLayoutParams(layoutParams);
        mIndicatorView.setBackgroundColor(getResources().getColor(R.color.theme_default_accent));
        addView(mIndicatorView);

        // indicator width
        mIndicatorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mIndicatorView.getViewTreeObserver().removeOnPreDrawListener(this);
                mIndicatorViewWidth = mIndicatorView.getWidth();
                return false;
            }
        });

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
//        Random rnd = new Random();
//        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//        mIndicatorView.setBackgroundColor(color);

        if (mCount > 0) {
            int transitionX = ((mDeviceWidth - mIndicatorViewWidth) / (mCount - 1)) * position;
            ObjectAnimator translationAnim =
                    ObjectAnimator.ofFloat(mIndicatorView, "translationX", transitionX);
            translationAnim.setDuration(ANIMATION_DURATION);
            translationAnim.setInterpolator(
                    AnimationFactory.makeDefaultReversePathInterpolator(getContext()));
            translationAnim.start();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
