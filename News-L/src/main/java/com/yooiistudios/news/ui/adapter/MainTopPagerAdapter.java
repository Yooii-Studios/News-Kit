package com.yooiistudios.news.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.ui.fragment.MainNewsFeedFragment;

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

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onTopItemClick(MainNewsFeedFragment.ItemViewHolder viewHolder,
                                   NewsFeed newsFeed, int position);
    }

    public MainTopPagerAdapter(FragmentManager fm, OnItemClickListener listener) {
        super(fm);
        mFragmentSparseArray = new SparseArray<MainNewsFeedFragment>();

        mOnItemClickListener = listener;
    }

    @Override
    public Fragment getItem(int i) {
//        NLTopNewsFeedViewPagerItem item = new NLTopNewsFeedViewPagerItem();
        MainNewsFeedFragment item =
                MainNewsFeedFragment.newInstance(mNewsFeed,
                        mNewsFeed.getNewsList().get(i), i);
        item.setOnItemClickListener(mOnItemClickListener);
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
