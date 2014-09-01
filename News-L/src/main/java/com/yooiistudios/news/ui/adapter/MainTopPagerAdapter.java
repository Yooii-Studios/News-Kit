package com.yooiistudios.news.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.ui.fragment.MainNewsFeedFragment;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * MainTopPagerAdapter
 *  메인화면 상단의 뷰페이저에 쓰이는 어댑터
 */
public class MainTopPagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<MainNewsFeedFragment> mFragmentSparseArray;
    private NewsFeed mNewsFeed;

    public MainTopPagerAdapter(FragmentManager fm, NewsFeed newsFeed) {
        super(fm);
        mFragmentSparseArray = new SparseArray<MainNewsFeedFragment>();
        mNewsFeed = newsFeed;
    }

    @Override
    public Fragment getItem(int i) {
//        NLTopNewsFeedViewPagerItem item = new NLTopNewsFeedViewPagerItem();
        MainNewsFeedFragment item =
                MainNewsFeedFragment.newInstance(mNewsFeed,
                        mNewsFeed.getNewsList().get(i), i);
        mFragmentSparseArray.put(i, item);

        return item;
    }

    @Override
    public int getCount() {
        return mNewsFeed.getNewsList().size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        MainNewsFeedFragment frag = mFragmentSparseArray.get(position);
        if (frag != null) {
            frag.setRecycled(true);
        }
        mFragmentSparseArray.remove(position);
    }

    public void notifyImageLoaded(int position) {
        MainNewsFeedFragment item = mFragmentSparseArray.get(position);
        if (item != null) {
            item.applyImage();
        }
    }

}
