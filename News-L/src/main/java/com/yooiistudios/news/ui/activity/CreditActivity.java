package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.adapter.CreditListAdapter;
import com.yooiistudios.news.util.MNAnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CreditActivity extends Activity {
    private static final String TAG = "CreditActivity";
    @InjectView(R.id.info_credit_list_view) ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            setContentView(R.layout.activity_credit);
            ButterKnife.inject(this);
            mListView.setAdapter(new CreditListAdapter(this));
            MNAnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
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
