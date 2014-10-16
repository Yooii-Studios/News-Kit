package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.ui.adapter.NewsSelectPagerAdapter;
import com.yooiistudios.news.ui.widget.viewpager.SlidingTabLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectActivity extends Activity {

    @InjectView(R.id.news_select_top_view_pager)    ViewPager mViewPager;
    @InjectView(R.id.news_select_sliding_tabs)      SlidingTabLayout mSlidingTabLayout;
    @InjectView(R.id.news_select_adView)            AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_select);
        ButterKnife.inject(this);

        Context context = getApplicationContext();

        mViewPager.setAdapter(new NewsSelectPagerAdapter(getFragmentManager(), context));
//        mViewPager.setOnPageChangeListener(mSimpleOnPageChangeListener);

        mSlidingTabLayout.setViewPager(mViewPager);

        initAdView();
    }

    private void initAdView() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        if (IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }
}
