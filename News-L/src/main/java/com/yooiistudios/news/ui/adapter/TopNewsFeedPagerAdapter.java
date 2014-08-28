package com.yooiistudios.news.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.ui.fragment.TopNewsFeedFragment;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 */
public class TopNewsFeedPagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<TopNewsFeedFragment> mFragmentSparseArray;
    private NewsFeed mNewsFeed;

    public TopNewsFeedPagerAdapter(FragmentManager fm, NewsFeed newsFeed) {
        super(fm);
        mFragmentSparseArray = new SparseArray<TopNewsFeedFragment>();
        mNewsFeed = newsFeed;
    }

    @Override
    public Fragment getItem(int i) {
//        NLTopNewsFeedViewPagerItem item = new NLTopNewsFeedViewPagerItem();
        TopNewsFeedFragment item =
                TopNewsFeedFragment.newInstance(mNewsFeed,
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

        TopNewsFeedFragment frag = mFragmentSparseArray.get(position);
        if (frag != null) {
            frag.setRecycled(true);
        }
        mFragmentSparseArray.remove(position);
    }

    public void notifyImageLoaded(Context context, int position) {
        TopNewsFeedFragment item = mFragmentSparseArray.get(position);
        if (item != null) {
            item.applyImage(context);
        }
    }

}
