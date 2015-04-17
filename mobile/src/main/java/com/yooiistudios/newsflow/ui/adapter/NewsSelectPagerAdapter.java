package com.yooiistudios.newsflow.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.curation.NewsContentProvider;
import com.yooiistudios.newsflow.core.news.curation.NewsProviderLangType;
import com.yooiistudios.newsflow.ui.fragment.NewsSelectFragment;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectPagerAdapter
 *  뉴스 선택화면을 구성하는 뷰페이저 어댑터
 */
public class NewsSelectPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private NewsFeed mNewsFeed;

    public NewsSelectPagerAdapter(FragmentManager fm, Context context, NewsFeed newsFeed) {
        super(fm);
        mContext = context;
        mNewsFeed = newsFeed;
    }

    @Override
    public Fragment getItem(int i) {
        return NewsSelectFragment.newInstance(i, mNewsFeed);
    }

    @Override
    public int getCount() {
        return NewsProviderLangType.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return NewsContentProvider.getInstance(mContext).getNewsLanguageTitle(position);
    }
}
