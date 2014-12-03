package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.ui.adapter.NewsSelectPagerAdapter;
import com.yooiistudios.news.ui.fragment.CustomNewsFeedDialogFragment;
import com.yooiistudios.news.ui.fragment.NewsSelectFragment;
import com.yooiistudios.news.ui.widget.viewpager.SlidingTabLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectActivity extends Activity
        implements CustomNewsFeedDialogFragment.OnClickListener {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.news_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_custom_news_feed:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = CustomNewsFeedDialogFragment.newInstance();
                newFragment.show(ft, "dialog");

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfirm(NewsFeedUrl feedUrl) {
        NewsFeed newsFeed = new NewsFeed();
        newsFeed.setNewsFeedUrl(feedUrl);
        getIntent().putExtra(NewsSelectFragment.KEY_SELECTED_NEWS_FEED, newsFeed);
        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }

    @Override
    public void onCancel() {

    }
}
