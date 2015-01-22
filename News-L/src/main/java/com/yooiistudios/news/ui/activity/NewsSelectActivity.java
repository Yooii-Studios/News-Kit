package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.ui.adapter.NewsSelectPagerAdapter;
import com.yooiistudios.news.ui.fragment.CustomRssDialogFragment;
import com.yooiistudios.news.ui.fragment.NewsSelectFragment;
import com.yooiistudios.news.ui.widget.viewpager.SlidingTabLayout;
import com.yooiistudios.news.util.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectActivity extends ActionBarActivity
        implements CustomRssDialogFragment.OnActionListener {
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

        mViewPager.setAdapter(new NewsSelectPagerAdapter(getFragmentManager()));
//        mViewPager.setOnPageChangeListener(mSimpleOnPageChangeListener);

        mSlidingTabLayout.setViewPager(mViewPager);

        initToolbar();
        initSlidingTab();
        initAdView();
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.Base_TextAppearance_AppCompat_Title);
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
    public void onPositive(NewsFeedUrl feedUrl) {
        getIntent().putExtra(NewsSelectFragment.KEY_CUSTOM_RSS_URL, feedUrl);
        setResult(Activity.RESULT_OK, getIntent());
        finish();
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
}
