package com.yooiistudios.newskit.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.util.Device;
import com.yooiistudios.newskit.iab.IabProducts;
import com.yooiistudios.newskit.ui.fragment.NewsSelectCountryFragment;
import com.yooiistudios.newskit.ui.fragment.NewsSelectProviderFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectDetailActivity extends ActionBarActivity {
    public static final String KEY_IS_COUNTRY_SELECTED = "key_is_country_selected";
    public static final String KEY_NEWS_PROVIDER_COUNTRY = "key_news_provider_country";
    public static final String KEY_NEWS_PROVIDER = "key_news_provider";

    @InjectView(R.id.news_select_detail_toolbar) Toolbar mToolbar;
    @InjectView(R.id.news_select_detail_adView) AdView mAdView;

    private NewsFeed mCurrentNewsFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OS에 의해서 kill 당할 경우 복구하지 말고 바로 finish
        if (savedInstanceState != null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_news_select_detail);
        ButterKnife.inject(this);

        initNewsFeed();
        initToolbar();
        initFragment();
        initAdView();
    }

    private void initNewsFeed() {
        NewsFeed newsFeed = getIntent().getExtras().getParcelable(NewsFeed.KEY_NEWS_FEED);
        if (newsFeed != null) {
            Parcel parcel = Parcel.obtain();
            newsFeed.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            mCurrentNewsFeed = NewsFeed.CREATOR.createFromParcel(parcel);
            parcel.recycle();
        }
    }

    private void initToolbar() {
        initToolbarOnLollipop();

        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initToolbarOnLollipop() {
        if (Device.hasLollipop()) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
    }

    public void setToolbarTitle(String title) {
        // typeface 는 따로 설정 필요
        SpannableString spannableTitle = new SpannableString(title);
        spannableTitle.setSpan(new TypefaceSpan(getString(R.string.noto_sans_medium)), 0,
                spannableTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        setTitle(spannableTitle);
    }

    private void initFragment() {
        // 국가를 클릭할 경우 언론사들을, 언론사를 클릭할 경우는 토픽을 보여주기
        boolean isCountrySelected = getIntent().getBooleanExtra(KEY_IS_COUNTRY_SELECTED, true);
        if (isCountrySelected) {
            String jsonString = getIntent().getStringExtra(KEY_NEWS_PROVIDER_COUNTRY);
            getSupportFragmentManager().beginTransaction().replace(R.id.news_select_detail_container,
                    NewsSelectCountryFragment.newInstance(jsonString, mCurrentNewsFeed)).commit();
        } else {
            String jsonString = getIntent().getStringExtra(KEY_NEWS_PROVIDER);
            getSupportFragmentManager().beginTransaction().replace(R.id.news_select_detail_container,
                    NewsSelectProviderFragment.newInstance(jsonString, mCurrentNewsFeed)).commit();
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
