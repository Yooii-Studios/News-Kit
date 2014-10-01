package com.yooiistudios.news.ui.widget.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.news.ui.fragment.MainNewsFeedFragment;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 26.
 *
 * MainTopViewPager
 *  메인 상단에 사용되는 뷰페이저
 */
public class MainTopViewPager extends ViewPager {
    private static final float PARALLAX_SCROLL_RATIO = 0.47f;

    public MainTopViewPager(Context context) {
        super(context);
    }

    public MainTopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        // position 으로는 제대로 된 현재 페이지를 파악할 수가 없어서 사용을 하지 않게 변경
        int pageWidth = getWidth() + getPageMargin();

        // 동적으로 현재 페이지 계산
        int currentPageIndex;
        if (getScrollX() == 0) {
            currentPageIndex = 0;
        } else {
            currentPageIndex = getScrollX() / pageWidth;
        }

        // Calculate
        // 현재 페이지에서 스크롤 된 값만 파악
        int scrollX;
        if (currentPageIndex != 0) {
            scrollX = getScrollX() % pageWidth;
        } else {
            scrollX = getScrollX();
        }

        // 프래그먼트를 꺼내어 이미지뷰 얻기
        MainTopPagerAdapter adapter = (MainTopPagerAdapter) getAdapter();
        MainNewsFeedFragment currentFragment = adapter.getFragmentSparseArray().get(currentPageIndex);
        MainNewsFeedFragment nextFragment;

        // 중요: nextFragment
        // 미리 어느 정도 이미지를 움직여 놓고 그곳에서 천천이 다시 오른쪽으로 들어와서 최종적으로 딱 맞게 한다
        float currentFragTransition;
        float nextFragTransition;
        if (position >= currentPageIndex) {
            currentFragTransition = scrollX * PARALLAX_SCROLL_RATIO;

            nextFragment = adapter.getFragmentSparseArray().get(currentPageIndex + 1);
            nextFragTransition = pageWidth * PARALLAX_SCROLL_RATIO * -1.0f + scrollX * PARALLAX_SCROLL_RATIO;
            if (scrollX == 0) {
                nextFragTransition = 0; // 스크롤이 끝난 후엔 원래 위치로 돌려주기
                currentFragTransition = 0;
            }
        } else {
            currentFragTransition = (pageWidth - scrollX) * PARALLAX_SCROLL_RATIO * -1;

            nextFragment = adapter.getFragmentSparseArray().get(currentPageIndex - 1);
            nextFragTransition = pageWidth * PARALLAX_SCROLL_RATIO + (pageWidth - scrollX) * PARALLAX_SCROLL_RATIO * -1.f;
            if (scrollX == 0) {
                nextFragTransition = 0;
                currentFragTransition = 0;
            }
        }

        // Translation
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
