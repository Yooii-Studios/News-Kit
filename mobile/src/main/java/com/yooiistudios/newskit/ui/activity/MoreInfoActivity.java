package com.yooiistudios.newskit.ui.activity;

import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newskit.NewsApplication;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.ui.adapter.MoreInfoListAdapter;
import com.yooiistudios.newskit.util.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MoreInfoActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
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
    @InjectView(R.id.more_info_toolbar)   Toolbar mToolbar;

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
        initToolbar();
        initListView();
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.yooiistudios.com/apps/newskit/android/help.php"));
                startActivity(intent);
                break;
            }
            case LICENSE:
                startActivity(new Intent(this, LicenseActivity.class));
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
