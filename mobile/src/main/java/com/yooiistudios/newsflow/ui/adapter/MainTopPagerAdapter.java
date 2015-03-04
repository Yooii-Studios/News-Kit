package com.yooiistudios.newsflow.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.ui.fragment.MainNewsFeedFragment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * MainTopPagerAdapter
 *  메인화면 상단의 뷰페이저에 쓰이는 어댑터
 */
public class MainTopPagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<MainNewsFeedFragment> mFragmentSparseArray;
    private NewsFeed mNewsFeed;

    public interface OnItemClickListener extends Serializable {
        public void onTopItemClick(MainNewsFeedFragment.ItemViewHolder viewHolder,
                                   NewsFeed newsFeed, int position);
    }

    public MainTopPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentSparseArray = new SparseArray<>();
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
        ArrayList<News> newsList;
        return (mNewsFeed != null && (newsList = mNewsFeed.getNewsList()) != null)
                ? newsList.size() : 0;
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

    public void notifyImageUrlLoaded(int position) {
        MainNewsFeedFragment item = mFragmentSparseArray.get(position);
        if (item != null) {
            item.applyImage();
        }
    }

    public void setNewsFeed(NewsFeed newsFeed) {
        mFragmentSparseArray.clear();

        mNewsFeed = newsFeed;
        notifyDataSetChanged();
    }

    public NewsFeed getNewsFeed() {
        return mNewsFeed;
    }

    public SparseArray<MainNewsFeedFragment> getFragmentSparseArray() {
        return mFragmentSparseArray;
    }

//    public boolean isFirst
}
