package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.gson.Gson;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.news.NewsContentProvider;
import com.yooiistudios.newsflow.model.news.NewsFeedUrl;
import com.yooiistudios.newsflow.model.news.NewsProvider;
import com.yooiistudios.newsflow.model.news.NewsProviderCountry;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.ui.adapter.NewsSelectPagerAdapter;
import com.yooiistudios.newsflow.ui.adapter.NewsSelectRecyclerAdapter;
import com.yooiistudios.newsflow.ui.fragment.CustomRssDialogFragment;
import com.yooiistudios.newsflow.ui.widget.viewpager.SlidingTabLayout;
import com.yooiistudios.newsflow.util.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectActivity extends ActionBarActivity
        implements CustomRssDialogFragment.OnActionListener,
        NewsSelectRecyclerAdapter.OnSelectionListener {
    public static final int RC_NEWS_SELECT_DETAIL = 38451;
    public static final String KEY_RSS_FETCHABLE = "key_selected_rss_fetchable";
    private static final String TAG = NewsSelectActivity.class.getName();

    @InjectView(R.id.news_select_toolbar)           Toolbar mToolbar;
    @InjectView(R.id.news_select_sliding_tabs)      SlidingTabLayout mSlidingTabLayout;
    @InjectView(R.id.news_select_top_view_pager)    ViewPager mViewPager;
    @InjectView(R.id.news_select_adView)            AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_select);
        ButterKnife.inject(this);

        initViewPager();
        initSlidingTabLayout();
        initToolbar();
        initSlidingTab();
        initAdView();
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initViewPager() {
        // 먼저 현재 언어에 따른 소팅이 필요하다
        NewsContentProvider.getInstance(this).sortNewsProviderLanguage(this);
        mViewPager.setAdapter(new NewsSelectPagerAdapter(getFragmentManager(), this));
    }

    private void initSlidingTabLayout() {
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);
    }

    private void initSlidingTab() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSlidingTabLayout.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.news_select_color_primary));
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
                if (!IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_CUSTOM_RSS_URL)) {
                    Toast.makeText(this, R.string.iab_item_unavailable, Toast.LENGTH_LONG).show();
                    return true;
                }
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = CustomRssDialogFragment.newInstance();
                newFragment.show(ft, "dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        // Activity visible to user
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        // Activity no longer visible
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onSelectNewsProvider(NewsProvider newsProvider) {
        Intent intent = new Intent(this, NewsSelectDetailActivity.class);
        intent.putExtra(NewsSelectDetailActivity.KEY_IS_COUNTRY_SELECTED, false);

        Gson gson = new Gson();
        String jsonString = gson.toJson(newsProvider);
        intent.putExtra(NewsSelectDetailActivity.KEY_NEWS_PROVIDER, jsonString);

        startActivityForResult(intent, RC_NEWS_SELECT_DETAIL);
    }

    @Override
    public void onSelectNewsProviderCountry(NewsProviderCountry newsProviderCountry) {
        Intent intent = new Intent(this, NewsSelectDetailActivity.class);
        intent.putExtra(NewsSelectDetailActivity.KEY_IS_COUNTRY_SELECTED, true);

        Gson gson = new Gson();
        String jsonString = gson.toJson(newsProviderCountry);
        intent.putExtra(NewsSelectDetailActivity.KEY_NEWS_PROVIDER_COUNTRY, jsonString);

        startActivityForResult(intent, RC_NEWS_SELECT_DETAIL);
    }

    @Override
    public void onEnterCustomRss(NewsFeedUrl feedUrl) {
        getIntent().putExtra(KEY_RSS_FETCHABLE, feedUrl);
        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            switch (requestCode) {
                case RC_NEWS_SELECT_DETAIL:
                    NewsTopic newsTopic = (NewsTopic) extras.getSerializable(KEY_RSS_FETCHABLE);
                    if (newsTopic != null) {
                        getIntent().putExtra(KEY_RSS_FETCHABLE, newsTopic);
                        setResult(Activity.RESULT_OK, getIntent());
                    }
                    finish();
                    break;
            }
        }
    }
}
