package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.ui.adapter.InfoAdapter;
import com.yooiistudios.news.util.AnalyticsUtils;
import com.yooiistudios.news.util.FacebookUtils;
import com.yooiistudios.news.util.ReviewUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InfoActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "InfoActivity";

    public enum InfoItem {
        MORE_INFO(R.string.info_news_info),
        RATE(R.string.info_rate_this_app),
        LIKE_FACEBOOK(R.string.info_facebook_like),
        CREDIT(R.string.info_credit);

        private int mTitleResId;

        private InfoItem(int titleResId) {
            mTitleResId = titleResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }
    }

    @InjectView(R.id.info_list_view) ListView mListView;
    @InjectView(R.id.info_adView)    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OS에 의해서 kill 당할 경우 복구하지 말고 메인 액티비티를 새로 띄워줌
        if (savedInstanceState != null) {
            finish();
        } else {
            setContentView(R.layout.activity_info);
            ButterKnife.inject(this);
            initListView();
            initAdView();
            AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
        }
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

            case RATE:
                ReviewUtils.showReviewActivity(this);
                break;

            case LIKE_FACEBOOK:
                FacebookUtils.openYooiiPage(this);
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
}
