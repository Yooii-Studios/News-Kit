package com.yooiistudios.newskit.ui.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newskit.NewsApplication;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.model.news.CustomRssHistoryUtils;
import com.yooiistudios.newskit.ui.adapter.CustomRssAdapter;
import com.yooiistudios.newskit.util.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News-Kit from Yooii Studios Co., LTD. on 15. 5. 18.
 *
 * CustomRssActivity
 *  뉴스 선택 - 커스텀 RSS 전용 액티비티
 */
public class CustomRssActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = CustomRssActivity.class.getName();

    @InjectView(R.id.custom_rss_toolbar) Toolbar mToolbar;
    @InjectView(R.id.custom_rss_edittext) AppCompatEditText mEditText;
    @InjectView(R.id.custom_rss_edit_layout) RelativeLayout mEditLayout;
    @InjectView(R.id.custom_rss_listview) ListView mListView;

    private MenuItem mDoneItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OS에 의해서 kill 당할 경우 복구하지 말고 바로 finish - 언어와 관련된 이슈를 사전에 방지
        if (savedInstanceState != null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_custom_rss);
        ButterKnife.inject(this);

        initToolbar();
        initEditLayout();
        initListView();
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
            mEditLayout.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);
    }

    private void initEditLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mEditLayout.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mEditLayout.bringToFront();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mDoneItem.setVisible(true);
                } else {
                    mDoneItem.setVisible(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEditText.requestFocus();
    }

    private void initListView() {
        mListView.setAdapter(new CustomRssAdapter(this));
        mListView.setOnItemClickListener(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, R.id.action_custom_news_feed_ok, 0,
                R.string.ok);
        item.setIcon(R.drawable.ic_check_black_28dp);
        mDoneItem = item;
        mDoneItem.setVisible(false);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_custom_news_feed_ok){
            String url = makeUrlString();
            if (url != null) {
                CustomRssHistoryUtils.addUrlToHistory(this, url);
                getIntent().putExtra(NewsSelectActivity.KEY_CUSTOM_URL, url);
                setResult(Activity.RESULT_OK, getIntent());
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String makeUrlString() {
        String url = mEditText.getText()
                .toString().replaceAll("\\s+", "");

        // add "http://" if it's not entered.
        if (!url.toLowerCase().matches("^\\w+://.*")) {
            url = "http://" + url;
        }
        return url;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            String url = ((TextView) view.findViewById(R.id.custom_rss_item_title_textview))
                    .getText().toString();
            CustomRssHistoryUtils.addUrlToHistory(this, url);
            getIntent().putExtra(NewsSelectActivity.KEY_CUSTOM_URL, url);
            setResult(Activity.RESULT_OK, getIntent());
            finish();
        }
    }

    /*
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.custom_rss_remove_rss);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // -1 은 헤더 위치를 빼고 삭제하는 것
                CustomRssHistoryUtils.removeUrlAtIndex(CustomRssActivity.this, position - 1);
                initListView();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
        return true;
    }
    */
}
