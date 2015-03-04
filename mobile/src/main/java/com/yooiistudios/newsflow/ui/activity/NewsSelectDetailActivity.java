package com.yooiistudios.newsflow.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.ui.fragment.NewsSelectCountryFragment;
import com.yooiistudios.newsflow.ui.fragment.NewsSelectProviderFragment;
import com.yooiistudios.newsflow.core.util.Device;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectDetailActivity extends ActionBarActivity {
    public static final String KEY_IS_COUNTRY_SELECTED = "key_is_country_selected";
    public static final String KEY_NEWS_PROVIDER_COUNTRY = "key_news_provider_country";
    public static final String KEY_NEWS_PROVIDER = "key_news_provider";

    @InjectView(R.id.news_select_detail_toolbar) Toolbar mToolbar;

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

        initToolbar();
        initFragment();
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

        mToolbar.setTitle(spannableTitle);
    }

    private void initFragment() {
        // 국가를 클릭할 경우 언론사들을, 언론사를 클릭할 경우는 토픽을 보여주기
        boolean isCountrySelected = getIntent().getBooleanExtra(KEY_IS_COUNTRY_SELECTED, true);
        if (isCountrySelected) {
            String jsonString = getIntent().getStringExtra(KEY_NEWS_PROVIDER_COUNTRY);
            getSupportFragmentManager().beginTransaction().replace(R.id.news_select_detail_container,
                    NewsSelectCountryFragment.newInstance(jsonString)).commit();
        } else {
            String jsonString = getIntent().getStringExtra(KEY_NEWS_PROVIDER);
            getSupportFragmentManager().beginTransaction().replace(R.id.news_select_detail_container,
                    NewsSelectProviderFragment.newInstance(jsonString)).commit();
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
