package com.yooiistudios.newsflow.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.ui.adapter.InfoAdapter;
import com.yooiistudios.newsflow.util.AnalyticsUtils;
import com.yooiistudios.newsflow.util.RecommendUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InfoActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "InfoActivity";

    public enum InfoItem {
        MORE_INFO(R.string.info_news_info),
        RECOMMEND_TO_FRIENDS(R.string.recommend_to_friends),
        CREDIT(R.string.info_credit);

        private int mTitleResId;

        private InfoItem(int titleResId) {
            mTitleResId = titleResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }
    }

    @InjectView(R.id.info_list_view)    ListView mListView;
    @InjectView(R.id.info_adView)       AdView mAdView;
    @InjectView(R.id.info_toolbar)      Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OS에 의해서 kill 당할 경우 복구하지 말고 메인 액티비티를 새로 띄워줌
        if (savedInstanceState != null) {
            finish();
        } else {
            setContentView(R.layout.activity_info);
            ButterKnife.inject(this);
            initToolbar();
            initListView();
            initAdView();
            AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
        }
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);

        // typeface 는 따로 설정 필요
        SpannableString titleString = new SpannableString(mToolbar.getTitle());
        titleString.setSpan(new TypefaceSpan(getString(R.string.noto_sans_medium)), 0,
                titleString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mToolbar.setTitle(titleString);
    }

    private void initAdView() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        if (IabProducts.containsSku(this, IabProducts.SKU_NO_ADS)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void initListView() {
        InfoAdapter mInfoAdapter = new InfoAdapter(this);
        mListView.setAdapter(mInfoAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        InfoItem item = InfoItem.values()[position];
        switch (item) {
            case MORE_INFO:
                startActivity(new Intent(this, MoreInfoActivity.class));
                break;

            case RECOMMEND_TO_FRIENDS:
                RecommendUtils.showRecommendDialog(this);
                break;

            case CREDIT:
                startActivity(new Intent(this, CreditActivity.class));
                break;

            default:
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
