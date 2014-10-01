package com.yooiistudios.news.ui.widget.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.news.ui.fragment.MainNewsFeedFragment;
import com.yooiistudios.news.util.NLLog;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 26.
 *
 * MainTopViewPager
 *  메인 상단에 사용되는 뷰페이저
 */
public class MainTopViewPager extends ViewPager implements ViewPager.OnPageChangeListener {
    int mScrollState = 0;
    int mTargetPageIndex = 0;
    int mPrevPageIndex = 0;

    int mCurrentPageIndex = 0;

    public MainTopViewPager(Context context) {
        super(context);
    }

    public MainTopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
//        NLLog.now("position: " + position + " / offset: " + offset + " / offsetPixels: " + offsetPixels);

        // 오른쪽으로 갈 때는 바로 다음 position이 나오고, 왼쪽으로 갈 때는
        // 스크롤이 완료 되어야만 전 페이지가 됨

//        NLLog.now("currentItem: " +  getCurrentItem());
        int scrollX;
        if (position != 0) {
//            NLLog.now("scrollX: " + (getScrollX() % (getWidth() + getPageMargin())));
            scrollX = getScrollX() % (getWidth() + getPageMargin());
        } else {
//            NLLog.now("scrollX: " + getScrollX());
            scrollX = getScrollX();
        }
//        NLLog.now("l - oldl: " + (l - oldl));


        if (getAdapter() instanceof MainTopPagerAdapter) {
            MainTopPagerAdapter adapter = (MainTopPagerAdapter) getAdapter();
            MainNewsFeedFragment currentFragment = adapter.getFragmentSparseArray().get(mCurrentPageIndex);
            MainNewsFeedFragment nextFragment;

//            NLLog.now("current page: " + (getWidth() + getPageMargin()) * position);
//            NLLog.now("scrollX: " + getScrollX());
//            if ((getWidth() + getPageMargin()) * position > getScrollX()) {
            float currentFragTransition;
            float nextFragTransition;
            if (position >= mCurrentPageIndex) {
                NLLog.now("swipe left");
//                NLLog.now("scrollX: " + scrollX);
                nextFragment = adapter.getFragmentSparseArray().get(mCurrentPageIndex + 1);
                currentFragTransition = (float) (scrollX * 0.4);
//                transition = (float) (offsetPixels * 0.4);
                nextFragTransition = (float) (scrollX * 0.2 * -1);
            } else {
                NLLog.now("swipe right");
//                NLLog.now("getWidth() + getPageMargin() - scrollX: " + (getWidth() + getPageMargin() - scrollX));
                nextFragment = adapter.getFragmentSparseArray().get(mCurrentPageIndex - 1);
                currentFragTransition = (float) ((getWidth() + getPageMargin() - scrollX) * 0.4 * -1);
//                transition = (float) (scrollX * 0.4 * -1);
                nextFragTransition = (float) ((getWidth() + getPageMargin() - scrollX) * 0.2);
            }

            if (currentFragment != null && currentFragment.getView() != null) {
                ImageView imageView = (ImageView) currentFragment.getView().findViewById(R.id.main_top_feed_image_view);
                imageView.setTranslationX(currentFragTransition);
            }

            if (nextFragment != null && nextFragment.getView() != null) {
                ImageView imageView = (ImageView) nextFragment.getView().findViewById(R.id.main_top_feed_image_view);
                imageView.setTranslationX(nextFragTransition);
            }
        }
    }

    @Override
    public void onPageSelected(int i) {
//        NLLog.now("onPageSelected: " + i);
        mTargetPageIndex = i;
//        mCurrentPageIndex = i;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
//            NLLog.now("SCROLL_STATE_SETTLING");
//            NLLog.now("prev: " + mPrevPageIndex);
//            NLLog.now("target: " + mTargetPageIndex);
            mPrevPageIndex = mTargetPageIndex;
        } else if (state == SCROLL_STATE_IDLE){
            mCurrentPageIndex = mTargetPageIndex;
//            NLLog.now("SCROLL_STATE_IDLE: " + mCurrentPageIndex);
        }
        mScrollState = state;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

//        NLLog.now("l: " + l + " / t: " + t + " / oldl: " + oldl + " / oldt: " + oldt);
//        NLLog.now("l-oldl: " + (l - oldl));

//        // 현재의 칸을 파악하고, 좌우의 방향을 확인한 뒤 해당 두 프래그먼트를 얻어내고, 해당 이미지뷰의 위치를 이동시킨다.
//        mTopImageView.setTranslationY(scrollY * 0.4f);
    }
}
