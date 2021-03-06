package com.yooiistudios.newskit.ui.widget.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.newskit.ui.fragment.MainTopFragment;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 26.
 *
 * MainTopViewPager
 *  메인 상단에 사용되는 뷰페이저
 */
public class MainTopViewPager extends ViewPager {
    private static final float PARALLAX_SCROLL_RATIO = 0.55f; // 0.47f

    public MainTopViewPager(Context context) {
        super(context);
    }

    public MainTopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);

        if (getAdapter() == null) {
            return;
        }

        // position 으로는 제대로 된 현재 페이지를 파악할 수가 없어서 사용을 하지 않게 변경(마진 때문으로 판단)
        // 다시 로직을 수정했는데 position 으로 페이지를 파악하는데 별 문제가 없음.. 나중에 문제가 생길 경우 참고하자
        int pageWidth = getWidth() + getPageMargin();

        // 기존 로직: 회전할 때 아래 로직이 다 틀어지는 문제가 생겨 position 과 offsetPixels 를 다시 사용해서
        // 구현하게 변경했는데 아직은 문제가 없음. 레거시 코드는 혹시 나중을 위해 남겨둠
        // 동적으로 현재 페이지 계산, 오른쪽 페이지가 보이는 순간 currentPageIndex 가 +1이 됨
//        if (getScrollX() == 0) {
//            currentPageIndex = 0;
//        } else {
//            currentPageIndex = getScrollX() / pageWidth;
//        }

        // 현재 페이지에서 스크롤 된 값만 계산
        //        if (currentPageIndex != 0) {
//            scrollX = getScrollX() % pageWidth;
//        } else {
//            scrollX = getScrollX();
//        }

        // 프래그먼트를 꺼내어 이미지뷰 얻기
        MainTopPagerAdapter adapter = (MainTopPagerAdapter) getAdapter();
        MainTopFragment currentFragment = adapter.getFragmentSparseArray().get(position);
        MainTopFragment nextFragment;

        // 중요: nextFragment
        // 미리 어느 정도 이미지를 움직여 놓고 그곳에서 천천이 다시 오른쪽으로 들어와서 최종적으로 딱 맞게 한다
        float currentFragTransition;
        float nextFragTransition;

        currentFragTransition = offsetPixels * PARALLAX_SCROLL_RATIO;

        nextFragment = adapter.getFragmentSparseArray().get(position + 1);
        nextFragTransition = pageWidth * PARALLAX_SCROLL_RATIO * -1.0f +
                offsetPixels * PARALLAX_SCROLL_RATIO;
        if (offsetPixels == 0) {
            nextFragTransition = 0; // 스크롤이 끝난 후엔 원래 위치로 돌려주기
            currentFragTransition = 0;
        }

        // Translation
        if (currentFragment != null && currentFragment.getView() != null) {
            ImageView imageView = (ImageView) currentFragment.getView()
                    .findViewById(R.id.main_top_item_image_view);
            imageView.setTranslationX(currentFragTransition);
        }
        if (nextFragment != null && nextFragment.getView() != null) {
            ImageView imageView = (ImageView) nextFragment.getView()
                    .findViewById(R.id.main_top_item_image_view);
            imageView.setTranslationX(nextFragTransition);
        }
    }

    /*
        // 예전 로직인데 position < currentPageIndex 가 되는 경우가 없는데 이 때문에 문제가 생겨서 주석 처리
        // 일단 레거시 코드로 남겨서 지켜볼 예정
        if (position >= currentPageIndex) {
            NLLog.now("position >= currentPageIndex");
            currentFragTransition = scrollX * PARALLAX_SCROLL_RATIO;

            nextFragment = adapter.getFragmentSparseArray().get(currentPageIndex + 1);
            nextFragTransition = pageWidth * PARALLAX_SCROLL_RATIO * -1.0f + scrollX * PARALLAX_SCROLL_RATIO;
            if (scrollX == 0) {
                NLLog.now("scrollX == 0");
                nextFragTransition = 0; // 스크롤이 끝난 후엔 원래 위치로 돌려주기
                currentFragTransition = 0;
            }
        } else {
            NLLog.now("position < currentPageIndex");
            currentFragTransition = (pageWidth - scrollX) * PARALLAX_SCROLL_RATIO * -1;

            nextFragment = adapter.getFragmentSparseArray().get(currentPageIndex - 1);
            nextFragTransition = pageWidth * PARALLAX_SCROLL_RATIO + (pageWidth - scrollX) * PARALLAX_SCROLL_RATIO * -1.f;
            if (scrollX == 0) {
                NLLog.now("scrollX == 0");
                nextFragTransition = 0;
                currentFragTransition = 0;
            }
        }
    */
}
