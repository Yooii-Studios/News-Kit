package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.adapter.MoreInfoListAdapter;
import com.yooiistudios.news.util.MNAnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MoreInfoActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MoreInfoActivity";

    public enum MoreInfoItem {
        YOOII_STUDIOS(R.string.more_info_yooii_studios),
        HELP(R.string.more_info_news_help),
        LICENSE(R.string.more_info_license),
        VERSION(R.string.more_info_version);

        private int mTitleResId;

        private MoreInfoItem(int titleResId) {
            mTitleResId = titleResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }
    }

    @InjectView(R.id.more_info_list_view) ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OS에 의해서 kill 당할 경우 복구하지 말고 메인 액티비티를 새로 띄워줌
        if (savedInstanceState != null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        setContentView(R.layout.activity_more_info);
        ButterKnife.inject(this);
        initListView();
        MNAnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initListView() {
        MoreInfoListAdapter mMoreInfoAdapter = new MoreInfoListAdapter(this);
        mListView.setAdapter(mMoreInfoAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MoreInfoItem item = MoreInfoItem.values()[position];
        switch (item) {
            case YOOII_STUDIOS: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://yooiistudios.com"));
                startActivity(intent);
                break;
            }
            case HELP: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.yooiistudios.com/apps/morning/ios/help.php"));
                startActivity(intent);
                break;
            }
            case LICENSE:
                break;

            case VERSION:
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
