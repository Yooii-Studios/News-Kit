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
    private static final float RATIO = 0.45f;
    int mTargetPageIndex = 0;
    int mCurrentPageIndex = 0;
    int mScrollState = SCROLL_STATE_IDLE;

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
//        NLLog.now("position: " + position);

        // 오른쪽으로 갈 때는 바로 다음 position이 나오고, 왼쪽으로 갈 때는
        // 스크롤이 완료 되어야만 전 페이지가 됨

//        NLLog.now("currentItem: " +  getCurrentItem());
        // 현재 페이지에서 스크롤 된 값만 파악
        int pageWidth = getWidth() + getPageMargin();
        int scrollX;
        if (position != 0) {
            scrollX = getScrollX() % pageWidth;
        } else {
            scrollX = getScrollX();
        }

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
//                NLLog.now("swipe left");
//                NLLog.now("scrollX: " + scrollX);
                nextFragment = adapter.getFragmentSparseArray().get(mCurrentPageIndex + 1);
                currentFragTransition = scrollX * RATIO;
//                transition = (float) (offsetPixels * 0.4);

                // 중요: 미리 어느 정도 이미지를 움직여 놓고 그곳에서 천천이 다시 왼쪽으로 들어와서 최종적으로 딱 맞게 한다.
                nextFragTransition = pageWidth * RATIO * -1.0f + scrollX * RATIO;
                if (scrollX == 0) {
                    nextFragTransition = 0; // 마지막 스크롤 시에는 원래 위치로 돌려주기
                    currentFragTransition = 0;
                }
            } else {
//                NLLog.now("swipe right");
//                NLLog.now("getWidth() + getPageMargin() - scrollX: " + (getWidth() + getPageMargin() - scrollX));
                nextFragment = adapter.getFragmentSparseArray().get(mCurrentPageIndex - 1);
                currentFragTransition = (pageWidth - scrollX) * RATIO * -1;
//                transition = (float) (scrollX * 0.4 * -1);
//                nextFragTransition = (float) ((getWidth() + getPageMargin() - scrollX) * 0.4);

                // 중요: 미리 어느 정도 이미지를 움직여 놓고 그곳에서 천천이 다시 오른쪽으로 들어와서 최종적으로 딱 맞게 한다.
                nextFragTransition = pageWidth * 0.4f + (pageWidth - scrollX) * 0.4f * -1.f;
                if (scrollX == 0) {
                    nextFragTransition = 0; // 마지막 스크롤 시에는 원래 위치로 돌려주기
                    currentFragTransition = 0;
                }
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
        NLLog.now("onPageSelected: " + i);
        mTargetPageIndex = i;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // 스크롤이 멈추면 현재 인덱스로 설정
        if (state == SCROLL_STATE_IDLE) {
            mScrollState = state;
            NLLog.now("onPageScrollStateChanged: " + "SCROLL_STATE_IDLE");
            mCurrentPageIndex = mTargetPageIndex;
        } else if (state == SCROLL_STATE_SETTLING) {
            NLLog.now("onPageScrollStateChanged: " + "SCROLL_STATE_SETTLING");
        } else if (state == SCROLL_STATE_DRAGGING) {
            NLLog.now("onPageScrollStateChanged: " + "SCROLL_STATE_DRAGGING");
        }
    }
}
