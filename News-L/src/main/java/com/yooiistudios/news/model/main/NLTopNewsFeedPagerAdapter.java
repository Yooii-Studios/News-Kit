package com.yooiistudios.news.model.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.yooiistudios.news.model.news.NLNewsFeed;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 */
public class NLTopNewsFeedPagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<NLTopNewsFeedViewPagerItem> mFragmentSparseArray;
    private NLNewsFeed mNewsFeed;

    public NLTopNewsFeedPagerAdapter(FragmentManager fm, NLNewsFeed newsFeed) {
        super(fm);
        mFragmentSparseArray = new SparseArray<NLTopNewsFeedViewPagerItem>();
        mNewsFeed = newsFeed;
    }

    @Override
    public Fragment getItem(int i) {
//        NLTopNewsFeedViewPagerItem item = new NLTopNewsFeedViewPagerItem();
        NLTopNewsFeedViewPagerItem item =
                NLTopNewsFeedViewPagerItem.newInstance(mNewsFeed,
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

        NLTopNewsFeedViewPagerItem frag = mFragmentSparseArray.get(position);
        if (frag != null) {
            frag.setRecycled(true);
        }
        mFragmentSparseArray.remove(position);
    }

    public void notifyImageLoaded(Context context, int position) {
        NLTopNewsFeedViewPagerItem item = mFragmentSparseArray.get(position);
        if (item != null) {
            item.applyImage(context);
        }
    }

}
