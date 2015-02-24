package com.yooiistudios.newsflow.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.ui.fragment.NewsSelectDetailCountryFragment;
import com.yooiistudios.newsflow.util.Device;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectDetailActivity extends ActionBarActivity {
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_IS_COUNTRY_SELECTED = "key_is_country_selected";
    public static final String KEY_NEWS_PROVIDER_COUNTRY = "key_news_provider_country";

    @InjectView(R.id.news_select_detail_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_select_detail);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            initToolbar();
            initFragment();
        }
    }

    private void initToolbar() {
        initToolbarOnLollipop();

        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);

        // title 은 선택한 국가나 언론사를 표시
        String title = getIntent().getStringExtra(KEY_TITLE);
        if (title == null) {
            title = getString(R.string.select_newsfeed_title);
        }

        // typeface 는 따로 설정 필요
        SpannableString spannableTitle = new SpannableString(title);
        spannableTitle.setSpan(new TypefaceSpan(getString(R.string.noto_sans_medium)), 0,
                spannableTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mToolbar.setTitle(spannableTitle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initToolbarOnLollipop() {
        if (Device.hasLollipop()) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
    }

    private void initFragment() {
        // 국가를 클릭할 경우 언론사들을, 언론사를 클릭할 경우는 토픽을 보여주기
        boolean isCountrySelected = getIntent().getBooleanExtra(KEY_IS_COUNTRY_SELECTED, true);
        if (isCountrySelected) {
            String jsonString = getIntent().getStringExtra(KEY_NEWS_PROVIDER_COUNTRY);
            getSupportFragmentManager().beginTransaction().replace(R.id.news_select_detail_container,
                    NewsSelectDetailCountryFragment.newInstance(jsonString)).commit();
        } else {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.news_select_detail_container, new PlaceholderFragment())
//                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
