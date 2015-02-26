package com.yooiistudios.newsflow.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.yooiistudios.newsflow.model.news.NewsContentProvider;
import com.yooiistudios.newsflow.model.news.NewsProviderLangType;
import com.yooiistudios.newsflow.ui.fragment.NewsSelectFragment;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectPagerAdapter
 *  뉴스 선택화면을 구성하는 뷰페이저 어댑터
 */
public class NewsSelectPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public NewsSelectPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int i) {
        return NewsSelectFragment.newInstance(i);
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
